package handler.commands.strings;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;

public class INCRcommand implements Command {

    KeyValueStore keyValueStore = KeyValueStore.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 2) return "-ERR wrong number of arguments for 'INCR'\r\n";
        String key=commandContext.args.get(1);
        int expiry=-1;
        String value= keyValueStore.get(key);
        if(value==null){
            keyValueStore.set(key,"1", expiry);
            return ":1\r\n";
        }
        int val = 0;
        try {
            val = Integer.parseInt(value);
        }catch (NumberFormatException e){
            return "-ERR value is not an integer or out of range\r\n";
        }

        val=val+1;
        keyValueStore.set(key,Integer.toString(val), expiry);

        return ":"+val +"\r\n";
    }
}
