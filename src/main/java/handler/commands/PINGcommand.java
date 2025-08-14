package handler.commands;

import handler.Command;
import handler.CommandContext;

public class PINGcommand implements Command {

    @Override
    public String execute(CommandContext commandContext) {
        return "+PONG\r\n";
    }
}
