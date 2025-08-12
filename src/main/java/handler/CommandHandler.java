package handler;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public class CommandHandler {

    public static String handle(List<String> args, SocketChannel clientChannel) throws IOException {
        if (args.isEmpty()) {
            return "-ERR Empty command\r\n";
        }

        String commandName = args.get(0).toUpperCase();
        Command command = CommandRegistry.getCommand(commandName);

        if (command == null) {
            return "-ERR unknown command '" + commandName + "'\r\n";
        }

        return command.execute(args,clientChannel);
    }
}
