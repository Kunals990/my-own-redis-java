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

    public static String handle(CommandContext cmdContext) throws IOException {
        if (cmdContext.args.isEmpty()) {
            return "-ERR Empty command\r\n";
        }

        String commandName = cmdContext.args.get(0).toUpperCase();
        Command command = CommandRegistry.getCommand(commandName);
        CommandContext context = new CommandContext(cmdContext.args, cmdContext.clientChannel, cmdContext.clientState);

        if (cmdContext.clientState.inTransaction) {
            if (commandName.equalsIgnoreCase("EXEC")) {
                return command.execute(context);
            } else if (commandName.equalsIgnoreCase("DISCARD")) {
                cmdContext.clientState.transactionQueue.clear();
                cmdContext.clientState.inTransaction = false;
                return "+OK\r\n";
            } else if (commandName.equalsIgnoreCase("MULTI")) {
                return "-ERR MULTI calls can not be nested\r\n";
            } else {
                cmdContext.clientState.transactionQueue.add(cmdContext.args);
                return "+QUEUED\r\n";
            }
        } else {
            if (commandName.equalsIgnoreCase("MULTI")) {
                cmdContext.clientState.inTransaction = true;
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
