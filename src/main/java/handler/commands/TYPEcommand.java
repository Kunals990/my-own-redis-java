package handler.commands;

import handler.Command;
import store.KeyValueStore;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public class TYPEcommand implements Command {

    KeyValueStore keyValueStore = KeyValueStore.getInstance();

    @Override
    public String execute(List<String> args, SocketChannel clientChannel) throws IOException {
        if(args.size()<2) return "-ERR wrong number of arguments for 'TYPE'\r\n";

        String key = args.get(1).trim();

        String value = keyValueStore.get(key);

        if(value==null) return "+none\r\n";

        return "+string\r\n";
    }
}
