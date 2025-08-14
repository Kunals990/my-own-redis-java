package handler.commands;

import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class INFOcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 2) {
            return "-ERR wrong number of arguments for 'info' command\r\n";
        }
        String section = commandContext.args.get(1).toLowerCase();
        if ("replication".equals(section)) {
            String content = "role:master";
            return "$" + content.length() + "\r\n" + content + "\r\n";
        }
        return "$0\r\n\r\n";
    }
}
