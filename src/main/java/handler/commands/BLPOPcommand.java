package handler.commands;

import handler.BlockingClientManager;
import handler.Command;
import store.ListStore;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public class BLPOPcommand implements Command {

    private final ListStore listStore = ListStore.getInstance();
    private final BlockingClientManager blockingManager = BlockingClientManager.getInstance();

    @Override
    public String execute(List<String> args, SocketChannel clientChannel) throws IOException {
        if (args.size() < 3) {
            return "-ERR wrong number of arguments for 'blpop' command\r\n";
        }
        String timeoutStr = args.get(args.size() - 1);
        double timeoutSeconds;
        try {
            timeoutSeconds = Double.parseDouble(timeoutStr);
        } catch (NumberFormatException e) {
            return "-ERR timeout is not a valid float\r\n";
        }

        List<String> keys = args.subList(1, args.size() - 1);

        for (String key : keys) {
            List<String> list = listStore.getList(key);
            if (list != null && !list.isEmpty()) {
                String element = list.removeFirst();
                return "*2\r\n" +
                        "$" + key.length() + "\r\n" + key + "\r\n" +
                        "$" + element.length() + "\r\n" + element + "\r\n";
            }
        }

        long timeoutMillis = timeoutSeconds <= 0 ? 0 : (long) (timeoutSeconds * 1000);

        for (String key : keys) {
            blockingManager.addBlockedClient(key, clientChannel, timeoutMillis);
        }
        return null;
    }
}