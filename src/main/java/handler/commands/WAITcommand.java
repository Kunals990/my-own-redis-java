package handler.commands;

import config.ReplicationInfo;
import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class WAITcommand implements Command {

    ReplicationInfo replicationInfo = ReplicationInfo.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (replicationInfo.getRole().equalsIgnoreCase("slave")) {
            return "-ERR WAIT command is not supported on replicas\r\n";
        }
        int replicaCount = replicationInfo.getReplicas().size();

        return ":" + replicaCount + "\r\n";
    }
}
