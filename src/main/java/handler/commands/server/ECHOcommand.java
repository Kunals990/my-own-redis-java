package handler.commands.server;

import handler.Command;
import handler.CommandContext;

public class ECHOcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) {
        if (commandContext.args.size() < 2) return "-ERR wrong number of arguments for 'echo'\r\n";
        String msg = commandContext.args.get(1);
        return "$" + msg.length() + "\r\n" + msg + "\r\n";
    }
}
