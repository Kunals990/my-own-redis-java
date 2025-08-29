package handler;

import config.ReplicationInfo;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Set;

public class CommandHandler {

    private static final Set<String> WRITE_COMMANDS = Set.of(
            "SET","DEL","LPUSH","RPUSH","XADD"
    );

    private static final Set<String> ALLOWED_IN_SUBSCRIBED_MODE = Set.of(
            "SUBSCRIBE", "UNSUBSCRIBE", "PSUBSCRIBE", "PUNSUBSCRIBE", "PING", "QUIT"
    );

    public static String handle(List<String> args, ClientState clientState,SocketChannel clientChannel) throws IOException {
        if (args.isEmpty()) {
            return "-ERR Empty command\r\n";
        }

        String commandName = args.get(0).toUpperCase();
        if (clientState.inSubscribedMode && !ALLOWED_IN_SUBSCRIBED_MODE.contains(commandName)) {
            return "-ERR Can't execute '" + args.get(0).toLowerCase() + "': only (P|S)SUBSCRIBE / (P|S)UNSUBSCRIBE / PING / QUIT / RESET are allowed in this context\r\n";
        }
        Command command = CommandRegistry.getCommand(commandName);
        CommandContext context = new CommandContext(args, clientChannel, clientState);

        if (clientState.inTransaction) {
            if (commandName.equalsIgnoreCase("EXEC")) {
                return command.execute(context);
            } else if (commandName.equalsIgnoreCase("DISCARD")) {
                clientState.transactionQueue.clear();
                clientState.inTransaction = false;
                return "+OK\r\n";
            } else if (commandName.equalsIgnoreCase("MULTI")) {
                return "-ERR MULTI calls can not be nested\r\n";
            } else {
                clientState.transactionQueue.add(args);
                return "+QUEUED\r\n";
            }
        } else {
            if (commandName.equalsIgnoreCase("MULTI")) {
                clientState.inTransaction = true;
                return command.execute(context);
            } else if (commandName.equals("EXEC") || commandName.equals("DISCARD")) {
                return "-ERR " + commandName + " without MULTI\r\n";
            } else {
                if (command == null) {
                    return "-ERR unknown command '" + commandName + "'\r\n";
                }

                String response = command.execute(context);

                if(WRITE_COMMANDS.contains(commandName)){
                    ReplicationInfo.getInstance().propagate(context.args);
                }

                return response;
            }

        }

    }
}
