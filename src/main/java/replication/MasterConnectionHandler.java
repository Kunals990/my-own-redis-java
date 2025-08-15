package replication;

import config.ServerContext;
import handler.MasterConnectionState;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class MasterConnectionHandler implements Runnable {
    private final String masterHost;
    private final int masterPort;
    private final ServerContext serverContext;

    public MasterConnectionHandler(String masterHost, int masterPort, ServerContext serverContext) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.serverContext = serverContext;
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
        System.out.println("Performing handshake: Sending PING to master...");

        // Step 1: Send PING
        String pingCommand = "*1\r\n$4\r\nPING\r\n";
        channel.socket().getOutputStream().write(pingCommand.getBytes());

    }
}