package handler;

import java.nio.channels.SocketChannel;

public class WaitContext {
    public final SocketChannel clientChannel;
    public final int requiredReplicas;
    public final long timeoutMillis;
    public final int targetOffset;
    public final long startTime;

    public WaitContext(SocketChannel clientChannel, int requiredReplicas, long timeoutMillis, int targetOffset) {
        this.clientChannel = clientChannel;
        this.requiredReplicas = requiredReplicas;
        this.timeoutMillis = timeoutMillis;
        this.targetOffset = targetOffset;
        this.startTime = System.currentTimeMillis();
    }
}