package handler;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockingClientManager {
    private static final BlockingClientManager instance = new BlockingClientManager();

    private final Map<String, Queue<BlockedClient>> waitingClients = new ConcurrentHashMap<>();

    public static BlockingClientManager getInstance() {
        return instance;
    }

    public void addBlockedClient(String key, SocketChannel clientChannel, long timeoutMillis) {
        Queue<BlockedClient> queue = waitingClients.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>());
        queue.add(new BlockedClient(clientChannel, System.currentTimeMillis(), timeoutMillis));
    }

    public BlockedClient getNextClientToUnblock(String key) {
        Queue<BlockedClient> queue = waitingClients.get(key);
        if (queue != null) {
            BlockedClient client = queue.poll();

            if (queue.isEmpty()) {
                waitingClients.remove(key, queue);
            }
            return client;
        }
        return null;
    }

    public Map<String, Queue<BlockedClient>> getWaitingClientsMap() {
        return waitingClients;
    }
}