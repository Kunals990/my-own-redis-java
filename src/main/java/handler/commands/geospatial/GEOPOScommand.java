package handler.commands.geospatial;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;
import utils.GeoHash;

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

            // 1. Get the member's score (the geohash) from the store
            Double score = KeyValueStore.getInstance().zscore(key, member);

            if (score != null) {
                GeoHash.Coordinates coords = GeoHash.decode(score.longValue());

                String lonStr = String.valueOf(coords.longitude);
                String latStr = String.valueOf(coords.latitude);

                response.append("*2\r\n");
                response.append("$").append(lonStr.length()).append("\r\n").append(lonStr).append("\r\n");
                response.append("$").append(latStr.length()).append("\r\n").append(latStr).append("\r\n");
            } else {
                response.append("*-1\r\n");
            }
        }

        return response.toString();
    }
}