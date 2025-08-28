package handler.commands;

import config.ReplicationInfo;
import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class REPLCONFcommand implements Command {

    ReplicationInfo replicationInfo = ReplicationInfo.getInstance();

    @Override
    public String execute(CommandContext commandContext) throws IOException {

        if(replicationInfo.getRole().equalsIgnoreCase("slave")){
            if(commandContext.args.get(1).equalsIgnoreCase("GETACK")){
                return "*3\r\n$8\r\nREPLCONF\r\n$3\r\nACK\r\n$1\r\n"+replicationInfo.getReplOffset()+"\r\n";
            }
            else{
                return null;
            }
        }

        return "+OK\r\n";
    }
}
