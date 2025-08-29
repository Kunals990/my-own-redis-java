package handler.commands.sets;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;

public class ZRANKcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 3) {
            return "-ERR wrong number of arguments for 'zrank' command\r\n";
        }

        String key = commandContext.args.get(1);
        String member = commandContext.args.get(2);

        Integer rank = KeyValueStore.getInstance().zrank(key, member);

        if (rank == null) {
            return "$-1\r\n";
        } else {
            return ":" + rank + "\r\n";
        }
    }
}
