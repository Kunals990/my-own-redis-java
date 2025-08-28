package handler.commands.strings;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;
import store.StreamStore;

import java.io.IOException;

public class TYPEcommand implements Command {

    KeyValueStore keyValueStore = KeyValueStore.getInstance();
    StreamStore streamStore = StreamStore.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if(commandContext.args.size()<2) return "-ERR wrong number of arguments for 'TYPE'\r\n";

        String key = commandContext.args.get(1).trim();

        if(streamStore.exists(key)){
            return "+stream\r\n";
        }

        String value = keyValueStore.get(key);

        if(value==null) return "+none\r\n";

        return "+string\r\n";
    }
}
