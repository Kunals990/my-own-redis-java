package config;

public class ReplicationInfo {
    private static final ReplicationInfo INSTANCE = new ReplicationInfo();

    private String role = "master"; // Default role is master
    private String masterHost;
    private int masterPort;

    private ReplicationInfo() {}

    public static ReplicationInfo getInstance() {
        return INSTANCE;
    }

    public String getRole() {
        return role;
    }

    public void setReplicaOf(String host, int port) {
        this.role = "slave";
        this.masterHost = host;
        this.masterPort = port;
    }
}