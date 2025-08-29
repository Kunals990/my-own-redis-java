package handler.commands.sets;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;

public class ZCARDcommand implements Command {

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 2) {
            return "-ERR wrong number of arguments for 'zcard' command\r\n";
        }

        String key = commandContext.args.get(1);

        int count = KeyValueStore.getInstance().zcard(key);

        return ":" + count + "\r\n";
    }
}
