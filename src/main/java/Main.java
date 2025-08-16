import config.ReplicationInfo;
import config.ServerContext;
import handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import parser.IncompleteCommandException;
import parser.ParseResult;
import parser.RESPParser;
import replication.MasterConnectionHandler;

public class Main {

    public static final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    private static Selector selector;


    public static void main(String[] args) throws IOException {
      int port = 6379;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port") && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (NumberFormatException e) {
                    System.err.println("Error: Invalid port number. Please provide an integer.");
                    return;
                }
            }
            else if (args[i].equals("--replicaof") && i + 1 < args.length) {
                String[] replicaOfArgs = args[i + 1].split(" ");
                if (replicaOfArgs.length != 2) {
                    System.err.println("Error: Invalid --replicaof format. Use '--replicaof <host> <port>'.");
                    return;
                }
                String masterHost = replicaOfArgs[0];
                try {
                    int masterPort = Integer.parseInt(replicaOfArgs[1]);
                    ReplicationInfo.getInstance().setReplicaOf(masterHost, masterPort);
                } catch (NumberFormatException e) {
                    System.err.println("Error: Invalid master port number in --replicaof.");
                    return;
                }
                i++;
            }
        }

      ServerSocketChannel serverChannel = ServerSocketChannel.open();
      serverChannel.configureBlocking(false);
      serverChannel.socket().bind(new InetSocketAddress(port));
      System.out.println("Event-loop server started on port " + port);

      Selector selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);

      ServerContext serverContext = new ServerContext(selector, taskQueue);
        ReplicationInfo replicationInfo = ReplicationInfo.getInstance();
        if ("slave".equals(replicationInfo.getRole())) {
            MasterConnectionHandler handler = new MasterConnectionHandler(
                    replicationInfo.getMasterHost(),
                    replicationInfo.getMasterPort(),
                    port,
                    serverContext
            );
            new Thread(handler).start();
        }

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
                  Object attachment = key.attachment();

                  // This block for handling regular clients is correct.
                  if (attachment instanceof ClientState) {
                      SocketChannel clientChannel = (SocketChannel) key.channel();
                      ClientState clientState = (ClientState) attachment;
                      ByteBuffer buffer = ByteBuffer.allocate(1024);

                      try {
                          int bytesRead = clientChannel.read(buffer);
                          if (bytesRead == -1) {
                              key.cancel(); clientChannel.close(); continue;
                          }

                          buffer.flip();
                          clientState.readBuffer.append(StandardCharsets.UTF_8.decode(buffer));

                          ParseResult result = RESPParser.parse(clientState.readBuffer.toString());
                          clientState.readBuffer.delete(0, result.getConsumedBytes());

                          for (List<String> commandParts : result.getCommands()) {
                              System.out.println("Parsed command: " + commandParts);
                              CommandContext cmdContext = new CommandContext(commandParts, clientChannel, clientState);
                              String response = CommandHandler.handle(cmdContext);
                              if (response != null) {
                                  clientChannel.write(ByteBuffer.wrap(response.getBytes()));
                              }
                          }
                      } catch (IncompleteCommandException e) {
                          // This is fine, just wait for more data.
                      } catch (Exception e) {
                          System.err.println("Error processing client command: " + e.getMessage());
                          key.channel().close();
                          key.cancel();
                      }

                  }
                  // This is the block that needs to be replaced.
                  else if (attachment instanceof MasterConnectionState) {
                      MasterConnectionState masterState = (MasterConnectionState) attachment;
                      SocketChannel masterChannel = (SocketChannel) key.channel();
                      ByteBuffer buffer = ByteBuffer.allocate(1024);

                      try {
                          int bytesRead = masterChannel.read(buffer);
                          if (bytesRead == -1) {
                              key.cancel(); masterChannel.close(); continue;
                          }

                          buffer.flip();
                          masterState.replicationBuffer.append(StandardCharsets.UTF_8.decode(buffer));

                          // --- STATE MACHINE LOGIC ---
                          boolean stillProcessing = true;
                          while(stillProcessing) {
                              switch (masterState.stage) {
                                  case WAITING_FOR_FULLRESYNC:
                                      int lineEnd = masterState.replicationBuffer.indexOf("\r\n");
                                      if (lineEnd != -1) {
                                          String line = masterState.replicationBuffer.substring(0, lineEnd);
                                          if (line.startsWith("+FULLRESYNC")) {
                                              System.out.println("FULLRESYNC response received, consuming it.");
                                              masterState.replicationBuffer.delete(0, lineEnd + 2);
                                              masterState.stage = SyncStage.WAITING_FOR_RDB;
                                          } else {
                                              stillProcessing = false;
                                          }
                                      } else {
                                          stillProcessing = false;
                                      }
                                      break;

                                  case WAITING_FOR_RDB:
                                      int rdbEndIndex = findRdbFileEnd(masterState.replicationBuffer);
                                      if (rdbEndIndex != -1) {
                                          System.out.println("RDB file received, consuming it.");
                                          masterState.replicationBuffer.delete(0, rdbEndIndex);
                                          masterState.stage = SyncStage.CONNECTED;
                                      } else {
                                          stillProcessing = false;
                                      }
                                      break;

                                  case CONNECTED:
                                      ParseResult result = RESPParser.parse(masterState.replicationBuffer.toString());
                                      if (result.getConsumedBytes() == 0) {
                                          stillProcessing = false;
                                          break;
                                      }
                                      masterState.replicationBuffer.delete(0, result.getConsumedBytes());

                                      for (List<String> commandParts : result.getCommands()) {
                                          System.out.println("Processing command from master: " + commandParts);
                                          String commandName = commandParts.get(0).toUpperCase();
                                          Command command = CommandRegistry.getCommand(commandName);
                                          if (command != null) {
                                              CommandContext context = new CommandContext(commandParts, masterChannel, null);
                                              command.execute(context);
                                          }
                                      }
                                      break;
                              }
                          }
                      } catch (IncompleteCommandException e) {
                          // This is normal, just wait for more data from the master.
                      } catch (Exception e) {
                          System.err.println("Error in replication stream: " + e.getMessage());
                          e.printStackTrace();
                      }
                  }
              }

          }

      }

  }

    private static int findRdbFileEnd(StringBuilder buffer) {
        if (buffer.length() == 0 || buffer.charAt(0) != '$') {
            return -1;
        }
        int lineEnd = buffer.indexOf("\r\n");
        if (lineEnd == -1) {
            return -1;
        }
        try {
            int length = Integer.parseInt(buffer.substring(1, lineEnd));
            int totalLength = lineEnd + 2 + length;
            if (buffer.length() >= totalLength) {
                return totalLength;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }
}


