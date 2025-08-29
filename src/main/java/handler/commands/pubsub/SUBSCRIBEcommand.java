package handler.commands.pubsub;

import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class SUBSCRIBEcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 2) return "-ERR wrong number of arguments for 'SUBSCRIBE'\r\n";
        String channelName = commandContext.args.get(1);

        int subscriptionCount = SubscriptionManager.getInstance().subscribe(
                channelName,
                commandContext.clientChannel
        );

        commandContext.clientState.inSubscribedMode = true;

        String channelResp = "$" + channelName.length() + "\r\n" + channelName + "\r\n";
        String countResp = ":" + subscriptionCount + "\r\n";

        return "*3\r\n$9\r\nsubscribe\r\n" + channelResp + countResp;
    }
}
