package handler.commands;

import config.ReplicationInfo;
import handler.Command;
import handler.CommandContext;
import handler.WaitContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WAITcommand implements Command {

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        ReplicationInfo replicationInfo = ReplicationInfo.getInstance();
        if (replicationInfo.getRole().equalsIgnoreCase("slave")) {
            return "-ERR WAIT command is not supported on replicas\r\n";
        }

        if (commandContext.args.size() < 3) {
            return "-ERR wrong number of arguments for 'wait' command\r\n";
        }

        int requiredReplicas = Integer.parseInt(commandContext.args.get(1));
        long timeout = Long.parseLong(commandContext.args.get(2));
        int targetOffset = replicationInfo.getMasterReplOffset();

        int connectedReplicas = replicationInfo.getReplicas().size();
        if (targetOffset == 0) {
            return ":" + connectedReplicas + "\r\n";
        }

        WaitContext waitContext = new WaitContext(
                commandContext.clientChannel,
                requiredReplicas,
                timeout,
                targetOffset
        );
        replicationInfo.addPendingWait(waitContext);

        String getAckCommand = "*3\r\n$8\r\nREPLCONF\r\n$6\r\nGETACK\r\n$1\r\n*\r\n";
        for (SocketChannel replica : replicationInfo.getReplicas()) {
            replica.write(ByteBuffer.wrap(getAckCommand.getBytes()));
        }
        return null;
    }
}