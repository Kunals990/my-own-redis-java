package config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReplicationInfo {
    private static final ReplicationInfo INSTANCE = new ReplicationInfo();

    private String role = "master"; // Default role is master
    private String masterHost;
    private int masterPort;
    private final String masterReplid = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    private int masterReplOffset = 0;

    private int replOffset = 0;

    private final List<SocketChannel> replicas = new CopyOnWriteArrayList<>();

    private ReplicationInfo() {}

    public static ReplicationInfo getInstance() {
        return INSTANCE;
    }

    public String getRole() {
        return role;
    }

    public String getMasterReplid() {
        return masterReplid;
    }

    public int getMasterReplOffset() {
        return masterReplOffset;
    }

    public void setReplicaOf(String host, int port) {
        this.role = "slave";
        this.masterHost = host;
        this.masterPort = port;
    }

    public String getMasterHost(){
        return masterHost;
    }

    public int getMasterPort(){
        return masterPort;
    }

    public void addReplica(SocketChannel replicaChannel) {
        replicas.add(replicaChannel);
    }

    public List<SocketChannel> getReplicas() {
        return replicas;
    }

    public void propagate(List<String> commandArgs) {
        List<SocketChannel> replicas = this.getReplicas();
        if (replicas.isEmpty()) {
            return;
        }

        String commandToPropagate = buildRespArray(commandArgs);
        System.out.println("Propagating to " + replicas.size() + " replicas: " + commandArgs);

        for (SocketChannel replicaChannel : replicas) {
            try {
                replicaChannel.write(ByteBuffer.wrap(commandToPropagate.getBytes()));
            } catch (IOException e) {
                System.err.println("Failed to propagate to replica: " + e.getMessage());
                // In a full implementation, disconnected replicas would be removed here
            }
        }
    }

    private String buildRespArray(List<String> args) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(args.size()).append("\r\n");
        for (String arg : args) {
            sb.append("$").append(arg.length()).append("\r\n");
            sb.append(arg).append("\r\n");
        }
        return sb.toString();
    }

    public int getReplOffset() {
        return replOffset;
    }

    public void setReplOffset(int replOffset) {
        this.replOffset = replOffset;
    }
}