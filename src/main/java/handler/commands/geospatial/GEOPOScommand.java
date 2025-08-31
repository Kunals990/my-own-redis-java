package handler.commands.geospatial;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;

import java.io.IOException;

public class GEOPOScommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() < 2) {
            return "-ERR wrong number of arguments for 'geopos' command\r\n";
        }

        String key = commandContext.args.get(1);
        int memberCount = commandContext.args.size() - 2;

        StringBuilder response = new StringBuilder();
        response.append("*").append(memberCount).append("\r\n");

        for (int i = 2; i < commandContext.args.size(); i++) {
            String member = commandContext.args.get(i);

            boolean exists = KeyValueStore.getInstance().geoMemberExists(key, member);

            if (exists) {
                response.append("*2\r\n");
                response.append("$1\r\n0\r\n");
                response.append("$1\r\n0\r\n");
            } else {
                response.append("*-1\r\n");
            }
        }

        return response.toString();
    }
}
