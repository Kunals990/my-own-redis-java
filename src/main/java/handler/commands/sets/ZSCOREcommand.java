package handler.commands.sets;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;

public class ZSCOREcommand implements Command {

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 3) {
            return "-ERR wrong number of arguments for 'zscore' command\r\n";
        }

        String key = commandContext.args.get(1);
        String member = commandContext.args.get(2);

        Double score = KeyValueStore.getInstance().zscore(key, member);

        if (score == null) {
            return "$-1\r\n";
        } else {
            String scoreStr = String.valueOf(score);
            return "$" + scoreStr.length() + "\r\n" + scoreStr + "\r\n";
        }
    }
}
