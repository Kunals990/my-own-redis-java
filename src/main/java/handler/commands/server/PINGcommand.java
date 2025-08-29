package handler.commands.server;

import config.ReplicationInfo;
import handler.ClientState;
import handler.Command;
import handler.CommandContext;

public class PINGcommand implements Command {

    @Override
    public String execute(CommandContext commandContext) {
        ClientState clientState = commandContext.clientState;
        if(clientState!=null && clientState.inSubscribedMode){
            return "*2\r\n$4\r\npong\r\n$0\r\n\r\n";
        }else if(ReplicationInfo.getInstance().getRole().equalsIgnoreCase("slave")){
            return null;
        }

        return "+PONG\r\n";
    }
}
