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
        return ":0\r\n";
    }
}
