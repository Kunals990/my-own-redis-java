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
        String scoreStr = commandContext.args.get(2);
        String member = commandContext.args.get(3);

        double scoreForStorage;
        try {
            long geohash = Long.parseLong(scoreStr);
            scoreForStorage = Double.longBitsToDouble(geohash);
        } catch (NumberFormatException e) {
            try {
                scoreForStorage = Double.parseDouble(scoreStr);
            } catch (NumberFormatException e2) {
                return "-ERR value is not a valid float\r\n";
            }
        }

        int newMembers = KeyValueStore.getInstance().zadd(key, scoreForStorage, member);
        return ":" + newMembers + "\r\n";
    }
}