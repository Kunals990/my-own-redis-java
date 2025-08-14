import handler.CommandHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import handler.TimeoutCheckerTask;
import parser.IncompleteCommandException;
import parser.ParseResult;
import parser.RESPParser;

public class Main {

    public static final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    private static Selector selector;

    public static void main(String[] args) throws IOException {
      int port = 6379;

      ServerSocketChannel serverChannel = ServerSocketChannel.open();
      serverChannel.configureBlocking(false);
      serverChannel.socket().bind(new InetSocketAddress(port));
      System.out.println("Event-loop server started on port " + port);

      Selector selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);

      ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            taskQueue.add(new TimeoutCheckerTask());
            selector.wakeup();
        }, 100, 100, TimeUnit.MILLISECONDS);

      //Event Loop
      while(true){
          selector.select(); // Wait until some channels are ready

          Runnable task;
          while ((task = taskQueue.poll()) != null) {
              task.run();
          }

          Set<SelectionKey> selectedKeys = selector.selectedKeys();
          Iterator<SelectionKey> iter = selectedKeys.iterator();

          while (iter.hasNext()){
              SelectionKey key = iter.next();
              iter.remove(); // Always remove the key once handled

              //Accept New Connections
              if (key.isAcceptable()) {
                  ServerSocketChannel server = (ServerSocketChannel) key.channel();
                  SocketChannel clientChannel = server.accept();

                  if (clientChannel != null) {
                      clientChannel.configureBlocking(false);
                      clientChannel.register(selector, SelectionKey.OP_READ,new ClientState());
                      System.out.println("Accepted new client: " + clientChannel.getRemoteAddress());
                  }
              }

              if (key.isReadable()) {
                  SocketChannel clientChannel = (SocketChannel) key.channel();
                  ClientState clientState = (ClientState) key.attachment();
                  ByteBuffer buffer = ByteBuffer.allocate(1024);

                  int bytesRead = -1;
                  try {
                      bytesRead = clientChannel.read(buffer);
                  } catch (IOException e) {
                      // Client closed unexpectedly
                      key.cancel();
                      clientChannel.close();
                      continue;
                  }

                  if (bytesRead == -1) {
                      // Client closed connection
                      System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
                      key.cancel();
                      clientChannel.close();
                      continue;
                  }

                  // Process the input
                  buffer.flip();
                  clientState.readBuffer.append(new String(buffer.array(), 0, buffer.limit()));
                  try{
                      ParseResult result = RESPParser.parse(clientState.readBuffer.toString());
                      clientState.readBuffer.delete(0, result.getConsumedBytes());

                      for (List<String> commandParts : result.getCommands()) {
                          System.out.println("Parsed command: " + commandParts);
                          String response = CommandHandler.handle(commandParts, clientChannel);
                          if (response != null) {
                              clientChannel.write(ByteBuffer.wrap(response.getBytes()));
                          }
                      }

                  }catch (IncompleteCommandException e){

                  }
                  catch (RuntimeException e) {
                      System.err.println("Error processing command: " + e.getMessage());
                      clientChannel.write(ByteBuffer.wrap("-ERR command processing failed\r\n".getBytes()));
                      clientState.readBuffer.setLength(0);
                  }
              }

          }

      }

  }
}


class ClientState {
    StringBuilder readBuffer = new StringBuilder();
}
