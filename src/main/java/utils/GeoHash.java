package utils;

import java.math.BigDecimal;
import java.math.MathContext;

public class GeoHash {
    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-85.05112878");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("85.05112878");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180.0");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180.0");

    private static final BigDecimal LATITUDE_RANGE = MAX_LATITUDE.subtract(MIN_LATITUDE);
    private static final BigDecimal LONGITUDE_RANGE = MAX_LONGITUDE.subtract(MIN_LONGITUDE);
    private static final BigDecimal FACTOR = BigDecimal.valueOf(Math.pow(2, 26));

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
        BigDecimal latBD = BigDecimal.valueOf(latitude);
        BigDecimal lonBD = BigDecimal.valueOf(longitude);

        BigDecimal normalizedLat = latBD.subtract(MIN_LATITUDE)
                .divide(LATITUDE_RANGE, MathContext.DECIMAL128)
                .multiply(FACTOR);

        BigDecimal normalizedLon = lonBD.subtract(MIN_LONGITUDE)
                .divide(LONGITUDE_RANGE, MathContext.DECIMAL128)
                .multiply(FACTOR);

        int latInt = normalizedLat.intValue();
        int lonInt = normalizedLon.intValue();

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

        BigDecimal latIntBD = BigDecimal.valueOf(latInt);
        BigDecimal lonIntBD = BigDecimal.valueOf(lonInt);

        BigDecimal lat = MIN_LATITUDE.add(LATITUDE_RANGE.multiply(
                latIntBD.add(new BigDecimal("0.5")).divide(FACTOR, MathContext.DECIMAL128)
        ));

        BigDecimal lon = MIN_LONGITUDE.add(LONGITUDE_RANGE.multiply(
                lonIntBD.add(new BigDecimal("0.5")).divide(FACTOR, MathContext.DECIMAL128)
        ));

        return new Coordinates(lat.doubleValue(), lon.doubleValue());
    }
}