package handler;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class BlockedStreamClient {
    public final SocketChannel channel;
    public final long expiryTime;

    public final Map<String, String> keysAndIds;

    public BlockedStreamClient(SocketChannel channel, Map<String, String> keysAndIds, long expiryTime) {
        this.channel = channel;
        this.keysAndIds = keysAndIds;
        this.expiryTime = expiryTime;
    }
}