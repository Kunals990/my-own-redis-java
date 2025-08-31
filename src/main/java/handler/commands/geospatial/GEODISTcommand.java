package handler.commands.geospatial;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;
import utils.GeoHash;

import java.io.IOException;

public class GEODISTcommand implements Command {
    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 4) {
            return "-ERR wrong number of arguments for 'geodist' command\r\n";
        }

        String key = commandContext.args.get(1);
        String member1 = commandContext.args.get(2);
        String member2 = commandContext.args.get(3);

        Double score1 = KeyValueStore.getInstance().zscore(key, member1);
        if (score1 == null) {
            return "$-1\r\n";
        }
        GeoHash.Coordinates coords1 = GeoHash.decode(score1.longValue());

        Double score2 = KeyValueStore.getInstance().zscore(key, member2);
        if (score2 == null) {
            return "$-1\r\n";
        }
        GeoHash.Coordinates coords2 = GeoHash.decode(score2.longValue());

        double distance = GeoHash.distance(coords1.latitude, coords1.longitude, coords2.latitude, coords2.longitude);

        String distStr = String.format("%.4f", distance);
        return "$" + distStr.length() + "\r\n" + distStr + "\r\n";
    }
}
