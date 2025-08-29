package handler.commands.pubsub;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionManager {
    private static final SubscriptionManager INSTANCE = new SubscriptionManager();

    // Key: Channel Name, Value: List of subscribed client channels
    private final Map<String, List<SocketChannel>> channelSubscriptions = new ConcurrentHashMap<>();

    // Key: Client Channel, Value: List of channels the client is subscribed to
    private final Map<SocketChannel, List<String>> clientSubscriptions = new ConcurrentHashMap<>();

    private SubscriptionManager() {}

    public static SubscriptionManager getInstance() {
        return INSTANCE;
    }

    public synchronized int subscribe(String channel, SocketChannel client) {
        channelSubscriptions.computeIfAbsent(channel, k -> new ArrayList<>()).add(client);

        List<String> subscriptions = clientSubscriptions.computeIfAbsent(client, k -> new ArrayList<>());
        if (!subscriptions.contains(channel)) {
            subscriptions.add(channel);
        }

        return subscriptions.size();
    }

    public synchronized int getSubscriberCount(String channel) {
        List<SocketChannel> subscribers = channelSubscriptions.get(channel);
        return (subscribers != null) ? subscribers.size() : 0;
    }
}