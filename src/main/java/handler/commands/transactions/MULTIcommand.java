package handler.commands.transactions;

import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class MULTIcommand implements Command {

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        return "+OK\r\n";
    }
}
