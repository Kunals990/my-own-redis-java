package handler.commands.lists;

import handler.BlockedClient;
import handler.BlockingClientManager;
import handler.Command;
import handler.CommandContext;
import store.ListStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class RPUSHcommand implements Command {

    ListStore listStore = ListStore.getInstance();
    private final BlockingClientManager blockingManager = BlockingClientManager.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if(commandContext.args.size()<3) return "-ERR wrong number of arguments for 'RPUSH'\r\n";

        String key=commandContext.args.get(1);
        List<String> values = commandContext.args.subList(2, commandContext.args.size());

        int newListLength = listStore.appendToList(key, values);

        BlockedClient clientToUnblock = blockingManager.getNextClientToUnblock(key);

        if (clientToUnblock != null) {
            String elementForBlockedClient = listStore.getList(key).removeFirst();

            String blpopResponse = "*2\r\n" +
                    "$" + key.length() + "\r\n" + key + "\r\n" +
                    "$" + elementForBlockedClient.length() + "\r\n" + elementForBlockedClient + "\r\n";
            try {
                clientToUnblock.channel.write(ByteBuffer.wrap(blpopResponse.getBytes()));
            } catch (IOException e) {
                System.err.println("Failed to write to unblocked client: " + e.getMessage());
            }
        }

        return ":" + newListLength + "\r\n";
    }
}
