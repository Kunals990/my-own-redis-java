package handler;

import protocols.RESPBuilder;
import store.StreamEntry;
import store.StreamStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockingClientManager {
    private static final BlockingClientManager instance = new BlockingClientManager();

    private final Map<String, Queue<BlockedClient>> waitingClients = new ConcurrentHashMap<>();
    private final Map<String, List<BlockedStreamClient>> waitingStreamClients = new ConcurrentHashMap<>();

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

    public void addBlockedStreamClient(Map<String, String> keysAndIds, SocketChannel channel, long timeoutMillis) {
        long expiryTime = timeoutMillis <= 0 ? 0 : System.currentTimeMillis() + timeoutMillis;
        BlockedStreamClient client = new BlockedStreamClient(channel, keysAndIds, expiryTime);

        for (String key : keysAndIds.keySet()) {
            waitingStreamClients.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>())).add(client);
        }
    }

    public void unblockClientsForStream(String streamKey) {
        List<BlockedStreamClient> waiters = waitingStreamClients.get(streamKey);
        if (waiters == null || waiters.isEmpty()) {
            return;
        }

        List<BlockedStreamClient> waitersCopy = new ArrayList<>(waiters);
        Set<BlockedStreamClient> unblockedClients = new HashSet<>();

        for (BlockedStreamClient client : waitersCopy) {
            if (unblockedClients.contains(client)) continue;

            Map<String, List<String>> availableData = findAvailableStreamData(client);

            if (!availableData.isEmpty()) {
                try {
                    String response = RESPBuilder.streamEntries(availableData);
                    if (client.channel.isOpen()) {
                        client.channel.write(ByteBuffer.wrap(response.getBytes()));
                    }
                    unblockedClients.add(client);
                } catch (IOException e) {
                    unblockedClients.add(client);
                }
            }
        }

        if (!unblockedClients.isEmpty()) {
            for (BlockedStreamClient clientToRemove : unblockedClients) {
                this.removeStreamClient(clientToRemove);
            }
        }
    }

    private Map<String, List<String>> findAvailableStreamData(BlockedStreamClient client) {
        Map<String, List<String>> availableEntries = new LinkedHashMap<>();
        StreamStore streamStore = StreamStore.getInstance();

        for (Map.Entry<String, String> request : client.keysAndIds.entrySet()) {
            String streamKey = request.getKey();
            String startId = request.getValue();

            List<String> entries = streamStore.readRangeAfter(streamKey, startId);

            if (!entries.isEmpty()) {
                availableEntries.put(streamKey, entries);
            }
        }
        return availableEntries;
    }

    public Map<String, List<BlockedStreamClient>> getWaitingStreamClientsMap() {
        return waitingStreamClients;
    }

    public void removeStreamClient(BlockedStreamClient clientToRemove) {
        for (String key : clientToRemove.keysAndIds.keySet()) {
            List<BlockedStreamClient> list = waitingStreamClients.get(key);
            if (list != null) {
                list.remove(clientToRemove);
                if (list.isEmpty()) {
                    waitingStreamClients.remove(key);
                }
            }
        }
    }
}