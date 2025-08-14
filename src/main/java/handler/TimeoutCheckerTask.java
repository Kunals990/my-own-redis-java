package handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

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
    }
}