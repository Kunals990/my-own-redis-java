package handler.commands;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class KEYScommand implements Command {

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 2 || !commandContext.args.get(1).equals("*")) {
            return "-ERR only KEYS * is supported\r\n";
        }

        Set<String> keys = KeyValueStore.getInstance().getAllKeys();

        StringBuilder response = new StringBuilder();
        response.append("*").append(keys.size()).append("\r\n");
        for (String key : keys) {
            response.append("$").append(key.length()).append("\r\n");
            response.append(key).append("\r\n");
        }

        System.out.println(keys);

        return response.toString();
    }
}