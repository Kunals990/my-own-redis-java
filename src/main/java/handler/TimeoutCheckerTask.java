package handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class TimeoutCheckerTask implements Runnable {

    @Override
    public void run() {
        BlockingClientManager manager = BlockingClientManager.getInstance();
        Map<String, Queue<BlockedClient>> clientMap = manager.getWaitingClientsMap();
        long now = System.currentTimeMillis();

        for (Queue<BlockedClient> queue : clientMap.values()) {
            Iterator<BlockedClient> iterator = queue.iterator();
            while (iterator.hasNext()) {
                BlockedClient client = iterator.next();

                if (client.timeoutMillis > 0 && (now - client.startTime >= client.timeoutMillis)) {
                    try {
                        if (client.channel.isOpen()) {
                            String nullResponse = "$-1\r\n";
                            client.channel.write(ByteBuffer.wrap(nullResponse.getBytes()));
                        }
                    } catch (IOException e) {
                        System.err.println("Error writing timeout response to client: " + e.getMessage());
                    } finally {
                        iterator.remove();
                    }
                }
            }
        }

        Map<String, List<BlockedStreamClient>> xreadMap = manager.getWaitingStreamClientsMap();
        Set<BlockedStreamClient> expiredClients = new HashSet<>();

        for (List<BlockedStreamClient> list : xreadMap.values()) {
            for (BlockedStreamClient client : list) {
                if (client.expiryTime > 0 && now >= client.expiryTime) {
                    expiredClients.add(client);
                }
            }
        }

        for (BlockedStreamClient expiredClient : expiredClients) {
            try {
                if (expiredClient.channel.isOpen()) {
                    // For XREAD timeout, response is a nil array
                    String nilResponse = "*-1\r\n";
                    expiredClient.channel.write(ByteBuffer.wrap(nilResponse.getBytes()));
                }
            } catch (IOException e) {
                System.err.println("Error writing XREAD timeout: " + e.getMessage());
            } finally {
                // Remove this expired client from ALL lists it was waiting on
                manager.removeStreamClient(expiredClient);
            }
        }
    }
}