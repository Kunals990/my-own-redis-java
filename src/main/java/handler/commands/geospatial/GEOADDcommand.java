package handler.commands.geospatial;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;
import utils.GeoHash;

import java.io.IOException;

public class GEOADDcommand implements Command {

    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final double MIN_LATITUDE = -85.05112878;
    private static final double MAX_LATITUDE = 85.05112878;

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        if (commandContext.args.size() != 5) {
            return "-ERR wrong number of arguments for 'geoadd' command\r\n";
        }

        double longitude;
        double latitude;

        try {
            longitude = Double.parseDouble(commandContext.args.get(2));
            latitude = Double.parseDouble(commandContext.args.get(3));
        } catch (NumberFormatException e) {
            return "-ERR value is not a valid float\r\n";
        }
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            return "-ERR invalid longitude argument\r\n";
        }
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            return "-ERR invalid latitude argument\r\n";
        }

        String key = commandContext.args.get(1);
        String member = commandContext.args.get(4);

        long geohashScore = GeoHash.encode(latitude, longitude);
        int newMembers = KeyValueStore.getInstance().zadd(key, geohashScore, member);

        return ":" + newMembers + "\r\n";
    }
}
