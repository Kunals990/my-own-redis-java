package config;

import java.nio.channels.Selector;
import java.util.Queue;

public class ServerContext {
    private final Selector selector;
    private final Queue<Runnable> taskQueue;
    // We could even move ReplicationInfo in here later for a super clean design.

    public ServerContext(Selector selector, Queue<Runnable> taskQueue) {
        this.selector = selector;
        this.taskQueue = taskQueue;
    }

    public Selector getSelector() {
        return selector;
    }

    public Queue<Runnable> getTaskQueue() {
        return taskQueue;
    }
}