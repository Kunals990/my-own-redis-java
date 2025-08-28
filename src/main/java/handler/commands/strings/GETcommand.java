package handler.commands.strings;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;

public class GETcommand implements Command {

    KeyValueStore store = KeyValueStore.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 2) return "-ERR wrong number of arguments for 'get'\r\n";

        String key=commandContext.args.get(1);
        String value = store.get(key);

        if (value == null) return "$-1\r\n";

        return "$" + value.length() + "\r\n" + value + "\r\n";
    }
}
