package handler.commands;

import config.ReplicationInfo;
import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class PSYNCcommand implements Command {

    ReplicationInfo replicationInfo = ReplicationInfo.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {

        if(commandContext.args.get(1).equals("?")){
            String fullResync = "+FULLRESYNC "+ replicationInfo.getMasterReplid()+" 0\r\n";
            return fullResync;
        }

        return null;
    }
}
