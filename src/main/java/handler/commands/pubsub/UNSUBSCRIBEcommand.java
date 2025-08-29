package handler.commands.pubsub;

import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class UNSUBSCRIBEcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 2) return "-ERR wrong number of arguments for 'UNSUBSCRIBE'\r\n";

        String channelName = commandContext.args.get(1);

        int subscriptionCount = SubscriptionManager.getInstance().unsubscribe(
                channelName,
                commandContext.clientChannel
        );

        if (subscriptionCount == 0) {
            commandContext.clientState.inSubscribedMode = false;
        }

        String channelResp = "$" + channelName.length() + "\r\n" + channelName + "\r\n";
        String countResp = ":" + subscriptionCount + "\r\n";

        return "*3\r\n$11\r\nunsubscribe\r\n" + channelResp + countResp;
    }
}
