package config;

public class ServerConfig {
    private static final ServerConfig INSTANCE = new ServerConfig();

    private String dir;
    private String dbfilename;

    private ServerConfig() {}

    public static ServerConfig getInstance() {
        return INSTANCE;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getDbfilename() {
        return dbfilename;
    }

    public void setDbfilename(String dbfilename) {
        this.dbfilename = dbfilename;
    }
}