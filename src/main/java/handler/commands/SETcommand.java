package handler.commands;

import config.ReplicationInfo;
import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;

public class SETcommand implements Command {

    KeyValueStore store = KeyValueStore.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 3) return "-ERR wrong number of arguments for 'set'\r\n";

        int expiry = -1; // -1 means no expiry

        if (commandContext.args.size() == 5 && commandContext.args.get(3).equalsIgnoreCase("PX")) {
            try {
                expiry = Integer.parseInt(commandContext.args.get(4));
            } catch (NumberFormatException e) {
                return "-ERR PX value is not a valid integer\r\n";
            }
        }

        String key = commandContext.args.get(1);
        String value = commandContext.args.get(2);

        store.set(key,value,expiry);

        if(ReplicationInfo.getInstance().getRole().equalsIgnoreCase("slave")){
            return null;
        }

        return "+OK\r\n";

    }
}
