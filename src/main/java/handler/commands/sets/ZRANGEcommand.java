package handler.commands.sets;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;
import java.util.List;

public class ZRANGEcommand implements Command {

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 4) {
            return "-ERR wrong number of arguments for 'zrange' command\r\n";
        }

        String key = commandContext.args.get(1);
        int start;
        int stop;

        try {
            start = Integer.parseInt(commandContext.args.get(2));
            stop = Integer.parseInt(commandContext.args.get(3));
        } catch (NumberFormatException e) {
            return "-ERR value is not an integer or out of range\r\n";
        }

        List<String> members = KeyValueStore.getInstance().zrange(key, start, stop);

        StringBuilder response = new StringBuilder();
        response.append("*").append(members.size()).append("\r\n");
        for (String member : members) {
            response.append("$").append(member.length()).append("\r\n");
            response.append(member).append("\r\n");
        }

        return response.toString();
    }
}
