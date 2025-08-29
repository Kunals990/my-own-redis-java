package handler.commands.sets;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;

public class ZADDcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 4) {
            return "-ERR wrong number of arguments for 'zadd' command\r\n";
        }
        String key = commandContext.args.get(1);
        String member = commandContext.args.get(3);
        double score;
        try {
            score = Double.parseDouble(commandContext.args.get(2));
        } catch (NumberFormatException e) {
            return "-ERR value is not a valid float\r\n";
        }
        int newMembers = KeyValueStore.getInstance().zadd(key, score, member);
        return ":" + newMembers + "\r\n";
    }
}
