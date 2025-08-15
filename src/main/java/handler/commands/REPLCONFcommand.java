package handler.commands;

import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class REPLCONFcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {

        return "+OK\r\n";
    }
}
