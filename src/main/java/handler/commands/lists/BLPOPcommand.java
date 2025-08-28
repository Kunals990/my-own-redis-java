package handler.commands.lists;

import handler.BlockingClientManager;
import handler.Command;
import handler.CommandContext;
import store.ListStore;

import java.io.IOException;
import java.util.List;

public class BLPOPcommand implements Command {

    private final ListStore listStore = ListStore.getInstance();
    private final BlockingClientManager blockingManager = BlockingClientManager.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 3) {
            return "-ERR wrong number of arguments for 'blpop' command\r\n";
        }
        String timeoutStr = commandContext.args.get(commandContext.args.size() - 1);
        double timeoutSeconds;
        try {
            timeoutSeconds = Double.parseDouble(timeoutStr);
        } catch (NumberFormatException e) {
            return "-ERR timeout is not a valid float\r\n";
        }

        List<String> keys = commandContext.args.subList(1, commandContext.args.size() - 1);

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
            blockingManager.addBlockedClient(key, commandContext.clientChannel, timeoutMillis);
        }
        return null;
    }
}