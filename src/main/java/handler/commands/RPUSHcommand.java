package handler.commands;

import handler.BlockedClient;
import handler.BlockingClientManager;
import handler.Command;
import store.ListStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public class RPUSHcommand implements Command {

    ListStore listStore = ListStore.getInstance();
    private final BlockingClientManager blockingManager = BlockingClientManager.getInstance();

    @Override
    public String execute(List<String> args, SocketChannel clientChannel) throws IOException {
        if(args.size()<3) return "-ERR wrong number of arguments for 'RPUSH'\r\n";

        String key=args.get(1);
        List<String> values = args.subList(2, args.size());

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
