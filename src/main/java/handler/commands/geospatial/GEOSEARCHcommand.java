package handler.commands.geospatial;

import handler.Command;
import handler.CommandContext;
import store.KeyValueStore;
import utils.GeoHash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GEOSEARCHcommand implements Command {

    private static class SearchResult {
        String member;
        double distance;
        GeoHash.Coordinates coordinates;
        long geoHash;

        SearchResult(String member, double distance, GeoHash.Coordinates coordinates, long geoHash) {
            this.member = member;
            this.distance = distance;
            this.coordinates = coordinates;
            this.geoHash = geoHash;
        }
    }

    @Override
    public String execute(CommandContext commandContext) throws IOException {
        List<String> args = commandContext.args;
        if (args.size() < 6) {
            return "-ERR wrong number of arguments for 'geosearch' command\r\n";
        }

        String key = args.get(1);
        double centerLon = 0, centerLat = 0;
        double radius = -1, boxWidth = -1, boxHeight = -1;
        String unit = "";
        String sort = null;
        int count = -1;
        boolean withCoord = false;
        boolean withDist = false;
        boolean withHash = false;
        boolean byRadius = false;
        boolean fromMember = false;

        int i = 2;
        try {
            String fromType = args.get(i++).toUpperCase();
            if ("FROMMEMBER".equals(fromType)) {
                String member = args.get(i++);
                Double score = KeyValueStore.getInstance().zscore(key, member);
                if (score == null) {
                    return "-ERR could not find member " + member + "\r\n";
                }
                GeoHash.Coordinates coords = GeoHash.decode(score.longValue());
                centerLon = coords.longitude;
                centerLat = coords.latitude;
            } else if ("FROMLONLAT".equals(fromType)) {
                centerLon = Double.parseDouble(args.get(i++));
                centerLat = Double.parseDouble(args.get(i++));
            } else {
                return "-ERR syntax error\r\n";
            }

            String byType = args.get(i++).toUpperCase();
            if ("BYRADIUS".equals(byType)) {
                byRadius = true;
                radius = Double.parseDouble(args.get(i++));
                unit = args.get(i++).toLowerCase();
            } else if ("BYBOX".equals(byType)) {
                byRadius = false;
                boxWidth = Double.parseDouble(args.get(i++));
                boxHeight = Double.parseDouble(args.get(i++));
                unit = args.get(i++).toLowerCase();
            } else {
                return "-ERR syntax error\r\n";
            }
            while (i < args.size()) {
                String option = args.get(i++).toUpperCase();
                switch (option) {
                    case "ASC":
                        sort = "ASC";
                        break;
                    case "DESC":
                        sort = "DESC";
                        break;
                    case "COUNT":
                        count = Integer.parseInt(args.get(i++));
                        break;
                    case "WITHCOORD":
                        withCoord = true;
                        break;
                    case "WITHDIST":
                        withDist = true;
                        break;
                    case "WITHHASH":
                        withHash = true;
                        break;
                    default:
                        return "-ERR syntax error\r\n";
                }
            }
        } catch (NumberFormatException e) {
            return "-ERR value is not a valid float\r\n";
        } catch (IndexOutOfBoundsException e) {
            return "-ERR syntax error\r\n";
        }

        Map<String, Double> allMembers = KeyValueStore.getInstance().getAllMembersWithScores(key);
        if (allMembers == null) {
            return "*0\r\n";
        }

        List<SearchResult> results = new ArrayList<>();
        double searchRadiusMeters = byRadius ? convertDistanceToMeters(radius, unit) : -1;
        double boxWidthMeters = !byRadius ? convertDistanceToMeters(boxWidth, unit) : -1;
        double boxHeightMeters = !byRadius ? convertDistanceToMeters(boxHeight, unit) : -1;


        for (Map.Entry<String, Double> entry : allMembers.entrySet()) {
            String member = entry.getKey();
            long geoHashCode = entry.getValue().longValue();
            GeoHash.Coordinates memberCoords = GeoHash.decode(geoHashCode);

            double distance = GeoHash.distance(centerLat, centerLon, memberCoords.latitude, memberCoords.longitude);

            boolean isMatch = false;
            if (byRadius) {
                if (distance <= searchRadiusMeters) {
                    isMatch = true;
                }
            } else {
                if (distance <= Math.sqrt(Math.pow(boxWidthMeters / 2, 2) + Math.pow(boxHeightMeters / 2, 2))) {
                    isMatch = true;
                }
            }

            if (isMatch) {
                results.add(new SearchResult(member, distance, memberCoords, geoHashCode));
            }
        }

        if (sort != null) {
            if ("ASC".equals(sort)) {
                results.sort(Comparator.comparingDouble(r -> r.distance));
            } else {
                results.sort((r1, r2) -> Double.compare(r2.distance, r1.distance));
            }
        }

        List<SearchResult> finalResults = results;
        if (count > 0 && results.size() > count) {
            finalResults = results.subList(0, count);
        }

        return formatResponse(finalResults, withCoord, withDist, withHash);
    }

    private double convertDistanceToMeters(double value, String unit) {
        switch (unit) {
            case "km":
                return value * 1000;
            case "ft":
                return value * 0.3048;
            case "mi":
                return value * 1609.34;
            case "m":
            default:
                return value;
        }
    }

    private String formatResponse(List<SearchResult> results, boolean withCoord, boolean withDist, boolean withHash) {
        StringBuilder response = new StringBuilder();
        response.append("*").append(results.size()).append("\r\n");

        for (SearchResult result : results) {
            int subArraySize = 1 + (withDist ? 1 : 0) + (withHash ? 1 : 0) + (withCoord ? 1 : 0);

            if (subArraySize == 1) {
                response.append("$").append(result.member.length()).append("\r\n").append(result.member).append("\r\n");
            } else {
                response.append("*").append(subArraySize).append("\r\n");
                response.append("$").append(result.member.length()).append("\r\n").append(result.member).append("\r\n");

                if (withDist) {
                    String distStr = String.format("%.4f", result.distance);
                    response.append("$").append(distStr.length()).append("\r\n").append(distStr).append("\r\n");
                }

                if (withHash) {
                    response.append(":").append(result.geoHash).append("\r\n");
                }

                if (withCoord) {
                    String lonStr = String.valueOf(result.coordinates.longitude);
                    String latStr = String.valueOf(result.coordinates.latitude);
                    response.append("*2\r\n");
                    response.append("$").append(lonStr.length()).append("\r\n").append(lonStr).append("\r\n");
                    response.append("$").append(latStr.length()).append("\r\n").append(latStr).append("\r\n");
                }
            }
        }
        return response.toString();
    }
}