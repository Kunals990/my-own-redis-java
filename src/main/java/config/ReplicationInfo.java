package config;

public class ReplicationInfo {
    private static final ReplicationInfo INSTANCE = new ReplicationInfo();

    private String role = "master"; // Default role is master
    private String masterHost;
    private int masterPort;
    private final String masterReplid = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    private int masterReplOffset = 0;

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
}