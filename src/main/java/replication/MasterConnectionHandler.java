package replication;

import config.ServerContext;
import handler.MasterConnectionState;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class MasterConnectionHandler implements Runnable {
    private final String masterHost;
    private final int masterPort;
    private final ServerContext serverContext;
    private final int replicaPort;

    public MasterConnectionHandler(String masterHost, int masterPort,  int replicaPort,ServerContext serverContext) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.serverContext = serverContext;
        this.replicaPort = replicaPort;
    }

    @Override
    public void run() {
        try {
            SocketChannel masterChannel = SocketChannel.open();
            masterChannel.connect(new InetSocketAddress(masterHost, masterPort));
            System.out.println("Successfully connected to master at " + masterHost + ":" + masterPort);

            performHandshake(masterChannel);

            masterChannel.configureBlocking(false);

            Runnable registrationTask = () -> {
                try {
                    masterChannel.register(serverContext.getSelector(), SelectionKey.OP_READ, new MasterConnectionState());
                } catch (ClosedChannelException e) {
                    System.err.println("Channel to master closed before registration.");
                }
            };

            serverContext.getTaskQueue().add(registrationTask);
            serverContext.getSelector().wakeup();

        } catch (IOException e) {
            System.err.println("Failed to connect or handshake with master: " + e.getMessage());
        }
    }

    private void performHandshake(SocketChannel channel) throws IOException {
        OutputStream outputStream = channel.socket().getOutputStream();
        InputStream inputStream = channel.socket().getInputStream();
        byte[] buffer = new byte[1024];

        System.out.println("Performing handshake: Sending PING to master...");
        String pingCommand = "*1\r\n$4\r\nPING\r\n";
        outputStream.write(pingCommand.getBytes());

        int bytesRead = inputStream.read(buffer);
        String pongResponse = new String(buffer, 0, bytesRead);
        System.out.println("Received from master: " + pongResponse.trim());
        if (!pongResponse.equalsIgnoreCase("+PONG\r\n")) {
            throw new IOException("Invalid response to PING: " + pongResponse);
        }

        System.out.println("Performing handshake: Sending REPLCONF listening-port...");
        String replconfPortCmd = buildRespArray("REPLCONF", "listening-port", String.valueOf(replicaPort));
        outputStream.write(replconfPortCmd.getBytes());

        bytesRead = inputStream.read(buffer);
        String okResponse1 = new String(buffer, 0, bytesRead);
        System.out.println("Received from master: " + okResponse1.trim());
        if (!okResponse1.equalsIgnoreCase("+OK\r\n")) {
            throw new IOException("Invalid response to REPLCONF port: " + okResponse1);
        }

        System.out.println("Performing handshake: Sending REPLCONF capa...");
        String replconfCapaCmd = buildRespArray("REPLCONF", "capa", "psync2");
        outputStream.write(replconfCapaCmd.getBytes());

        bytesRead = inputStream.read(buffer);
        String okResponse2 = new String(buffer, 0, bytesRead);
        System.out.println("Received from master: " + okResponse2.trim());
        if (!okResponse2.equalsIgnoreCase("+OK\r\n")) {
            throw new IOException("Invalid response to REPLCONF capa: " + okResponse2);
        }

        System.out.println("Handshake part 2 completed successfully.");

    }

    private String buildRespArray(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(args.length).append("\r\n");
        for (String arg : args) {
            sb.append("$").append(arg.length()).append("\r\n");
            sb.append(arg).append("\r\n");
        }
        return sb.toString();
    }
}