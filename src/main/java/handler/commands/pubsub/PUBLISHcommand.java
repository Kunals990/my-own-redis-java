package handler.commands.pubsub;

import handler.Command;
import handler.CommandContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public class PUBLISHcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 3) {
            return "-ERR wrong number of arguments for 'publish' command\r\n";
        }

        String channelName = commandContext.args.get(1);
        String message = commandContext.args.get(2);

        List<SocketChannel> subscribers = SubscriptionManager.getInstance().getSubscribers(channelName);

        String messageToDeliver = "*3\r\n" +
                "$7\r\nmessage\r\n" +
                "$" + channelName.length() + "\r\n" + channelName + "\r\n" +
                "$" + message.length() + "\r\n" + message + "\r\n";

        ByteBuffer buffer = ByteBuffer.wrap(messageToDeliver.getBytes());

        for (SocketChannel subscriber : subscribers) {
            try {
                subscriber.write(buffer.duplicate());
            } catch (IOException e) {
                System.err.println("Failed to publish to subscriber: " + e.getMessage());
            }
        }

        return ":" + subscribers.size() + "\r\n";
    }
}
