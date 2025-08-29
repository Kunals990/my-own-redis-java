package handler.commands.pubsub;

import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class PUBLISHcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 3) {
            return "-ERR wrong number of arguments for 'publish' command\r\n";
        }

        String channelName = commandContext.args.get(1);

        String message = commandContext.args.get(2);

        int subscriberCount = SubscriptionManager.getInstance().getSubscriberCount(channelName);

        return ":" + subscriberCount + "\r\n";
    }
}
