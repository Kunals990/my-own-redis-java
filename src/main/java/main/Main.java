package main;
import config.ReplicationInfo;
import config.ServerContext;
import handler.ClientState;
import handler.CommandHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import handler.TimeoutCheckerTask;
import handler.WaitContext;
import parser.IncompleteCommandException;
import parser.ParseResult;
import parser.RESPParser;
import replication.MasterConnectionHandler;

public class Main {

    public static final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    private static Selector selector;

    public static Selector getSelector() {
        return selector;
    }


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

      selector = Selector.open();
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
            checkPendingWaits();
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
                  if (attachment instanceof ClientState) {
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

                          for (ParseResult.CommandData commandData : result.getCommandDataList()) {
                              List<String> commandParts = commandData.getCommandParts();

                              System.out.println("Parsed command: " + commandParts);
                              String response = CommandHandler.handle(commandParts, clientState, clientChannel);
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
                  else if (attachment instanceof handler.ReplicaState) {
                      SocketChannel replicaChannel = (SocketChannel) key.channel();
                      ByteBuffer buffer = ByteBuffer.allocate(1024);

                      try {
                          int bytesRead = replicaChannel.read(buffer);
                          if (bytesRead == -1) {
                              key.cancel();
                              replicaChannel.close();
                              return;
                          }

                          buffer.flip();
                          String data = new String(buffer.array(), 0, buffer.limit());

                          ParseResult result = RESPParser.parse(data);
                          for (ParseResult.CommandData commandData : result.getCommandDataList()) {
                              List<String> commandParts = commandData.getCommandParts();
                              if (commandParts.get(0).equalsIgnoreCase("REPLCONF") && commandParts.get(1).equalsIgnoreCase("ACK")) {
                                  int offset = Integer.parseInt(commandParts.get(2));
                                  ReplicationInfo.getInstance().updateReplicaOffset(replicaChannel, offset);
                              }
                          }

                          checkPendingWaits();

                      } catch (IOException | IncompleteCommandException e) {
                          key.cancel();
                          replicaChannel.close();
                      }
                  }
                  else if (attachment instanceof handler.MasterConnectionState) {
                      SocketChannel masterChannel = (SocketChannel) key.channel();
                      handler.MasterConnectionState masterState = (handler.MasterConnectionState) attachment;
                      ByteBuffer buffer = ByteBuffer.allocate(1024);

                      int bytesRead = -1;
                      try {
                          bytesRead = masterChannel.read(buffer);
                      } catch (IOException e) {
                          System.err.println("Lost connection to master: " + e.getMessage());
                          key.cancel();
                          masterChannel.close();
                          continue;
                      }

                      if (bytesRead == -1) {
                          System.err.println("Master closed the connection.");
                          key.cancel();
                          masterChannel.close();
                          continue;
                      }

                      buffer.flip();
                      masterState.readBuffer.append(new String(buffer.array(), 0, buffer.limit()));

                      try {
                          ParseResult result = RESPParser.parse(masterState.readBuffer.toString());
                          masterState.readBuffer.delete(0, result.getConsumedBytes());

                          for (ParseResult.CommandData commandData : result.getCommandDataList()) {
                              List<String> commandParts = commandData.getCommandParts();
                              int commandSize = commandData.getCommandSize();

                              System.out.println("Replica executing command from master: " + commandParts);
                              String response = CommandHandler.handle(commandParts, new ClientState(), masterChannel);

                              if (response != null) {
                                  masterChannel.write(ByteBuffer.wrap(response.getBytes()));
                              }

                              ReplicationInfo.getInstance().incrementReplOffset(commandSize);
                          }
                      } catch (IncompleteCommandException e) {
                      } catch (Exception e) {
                          System.err.println("Error processing command from master: " + e.getMessage());
                          masterState.readBuffer.setLength(0);
                      }
                  }

              }

          }

      }

  }

    private static void checkPendingWaits() {
        ReplicationInfo replicationInfo = ReplicationInfo.getInstance();

        var iterator = replicationInfo.getPendingWaits().iterator();
        while (iterator.hasNext()) {
            WaitContext wait = iterator.next();

            int ackCount = replicationInfo.countSynchronizedReplicas(wait.targetOffset);
            boolean timeout = System.currentTimeMillis() - wait.startTime >= wait.timeoutMillis;

            if (ackCount >= wait.requiredReplicas || timeout) {
                try {
                    String response = ":" + ackCount + "\r\n";
                    wait.clientChannel.write(ByteBuffer.wrap(response.getBytes()));
                } catch (IOException e) {
                    System.err.println("Failed to send WAIT response to client: " + e.getMessage());
                } finally {
                    iterator.remove();
                }
            }
        }
    }


}


