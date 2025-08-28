package handler.commands;

import config.ReplicationInfo;
import handler.Command;
import handler.CommandContext;

public class PINGcommand implements Command {

    @Override
    public String execute(CommandContext commandContext) {
        if(ReplicationInfo.getInstance().getRole().equalsIgnoreCase("slave")){
            return null;
        }
        return "+PONG\r\n";
    }
}
