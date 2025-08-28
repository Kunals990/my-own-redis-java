package handler.commands.pubsub;

import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class SUBSCRIBEcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 2) return "-ERR wrong number of arguments for 'SUBSCRIBE'\r\n";
        String client = commandContext.args.get(1);

        return "*3\r\n$9\r\nsubscribe\r\n"+client.length()+"\r\n"+client+"\r\n:1\r\n";
    }
}
