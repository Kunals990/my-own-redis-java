package handler.commands.sets;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;

public class ZREMcommand implements Command {

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 3) {
            return "-ERR wrong number of arguments for 'zrem' command\r\n";
        }

        String key = commandContext.args.get(1);
        String member = commandContext.args.get(2);

        int removedCount = KeyValueStore.getInstance().zrem(key, member);

        return ":" + removedCount + "\r\n";
    }
}
