package utils;

public class GeoHash {
    private static final double MIN_LATITUDE = -85.05112878;
    private static final double MAX_LATITUDE = 85.05112878;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final double LATITUDE_RANGE = MAX_LATITUDE - MIN_LATITUDE;
    private static final double LONGITUDE_RANGE = MAX_LONGITUDE - MIN_LONGITUDE;

    private static long spreadInt32ToInt64(int v) {
        long result = v & 0xFFFFFFFFL;
        result = (result | (result << 16)) & 0x0000FFFF0000FFFFL;
        result = (result | (result << 8))  & 0x00FF00FF00FF00FFL;
        result = (result | (result << 4))  & 0x0F0F0F0F0F0F0F0FL;
        result = (result | (result << 2))  & 0x3333333333333333L;
        result = (result | (result << 1))  & 0x5555555555555555L;
        return result;
    }

    private static long interleave(int x, int y) {
        long xSpread = spreadInt32ToInt64(x);
        long ySpread = spreadInt32ToInt64(y);
        return xSpread | (ySpread << 1);
    }

    public static long encode(double latitude, double longitude) {
        double latFactor = Math.pow(2, 26) / LATITUDE_RANGE;
        double lonFactor = Math.pow(2, 26) / LONGITUDE_RANGE;
        int latInt = (int)((latitude - MIN_LATITUDE) * latFactor);
        int lonInt = (int)((longitude - MIN_LONGITUDE) * lonFactor);

        return interleave(latInt, lonInt);
    }

    public static class Coordinates {
        public final double latitude;
        public final double longitude;

        Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private static int compactInt64ToInt32(long v) {
        v &= 0x5555555555555555L;
        v = (v | (v >> 1))  & 0x3333333333333333L;
        v = (v | (v >> 2))  & 0x0F0F0F0F0F0F0F0FL;
        v = (v | (v >> 4))  & 0x00FF00FF00FF00FFL;
        v = (v | (v >> 8))  & 0x0000FFFF0000FFFFL;
        v = (v | (v >> 16)) & 0x00000000FFFFFFFFL;
        return (int) v;
    }

    public static Coordinates decode(long geoCode) {
        int latInt = compactInt64ToInt32(geoCode);
        int lonInt = compactInt64ToInt32(geoCode >> 1);

        double factor = Math.pow(2, 26);
        double latitude = MIN_LATITUDE + LATITUDE_RANGE * ((latInt + 0.5) / factor);
        double longitude = MIN_LONGITUDE + LONGITUDE_RANGE * ((lonInt + 0.5) / factor);

        return new Coordinates(latitude, longitude);
    }
}