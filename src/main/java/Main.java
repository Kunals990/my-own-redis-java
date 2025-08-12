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
import java.util.Set;
import parser.RESPParser;

public class Main {
  public static void main(String[] args) throws IOException {
      int port = 6379;

      ServerSocketChannel serverChannel = ServerSocketChannel.open();
      serverChannel.configureBlocking(false);
      serverChannel.socket().bind(new InetSocketAddress(port));
      System.out.println("Event-loop server started on port " + port);

      Selector selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);

      //Event Loop
      while(true){
          selector.select(); // Wait until some channels are ready

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
                      clientChannel.register(selector, SelectionKey.OP_READ);
                      System.out.println("Accepted new client: " + clientChannel.getRemoteAddress());
                  }
              }

              if (key.isReadable()) {
                  SocketChannel clientChannel = (SocketChannel) key.channel();
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
                  String input = new String(buffer.array(), 0, buffer.limit()).trim();
                  System.out.println("Received: " + input);

                  try{
                      List<String> commandParts = RESPParser.parse(input);
                      System.out.println("Parsed command: "+commandParts);

                      String response = CommandHandler.handle(commandParts,clientChannel);
                      if (response != null) {
                          clientChannel.write(ByteBuffer.wrap(response.getBytes()));
                      }

                  } catch (RuntimeException e) {
                      throw new RuntimeException(e);
                  }
              }

          }

      }

  }
}
