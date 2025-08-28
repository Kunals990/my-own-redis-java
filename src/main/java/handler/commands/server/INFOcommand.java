package handler.commands.server;

import config.ReplicationInfo;
import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class INFOcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 2) {
            return "-ERR wrong number of arguments for 'info' command\r\n";
        }
        String section = commandContext.args.get(1).toLowerCase();
        if ("replication".equals(section)) {
            ReplicationInfo replicationInfo = ReplicationInfo.getInstance();

            StringBuilder sb = new StringBuilder();
            sb.append("role:").append(replicationInfo.getRole()).append("\r\n");
            sb.append("master_replid:").append(replicationInfo.getMasterReplid()).append("\r\n");
            sb.append("master_repl_offset:").append(replicationInfo.getMasterReplOffset());

            String content = sb.toString();
            return "$" + content.length() + "\r\n" + content + "\r\n";
        }
        return "$0\r\n\r\n";
    }
}
