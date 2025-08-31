package handler.commands.geospatial;

import handler.Command;
import handler.CommandContext;

import java.io.IOException;

public class GEOADDcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 5) {
            return "-ERR wrong number of arguments for 'geoadd' command\r\n";
        }
        return ":1\r\n";
    }
}
