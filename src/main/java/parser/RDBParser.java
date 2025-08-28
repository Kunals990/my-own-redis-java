package parser;

import store.KeyValueStore;

public class RDBParser {
    private final byte[] rdbBytes;
    private int pointer = 0;

    public RDBParser(byte[] rdbBytes) {
        this.rdbBytes = rdbBytes;
    }

    public void parse() {
        // Step 1: Parse Header - Skip "REDIS" + 4-digit version number
        pointer += 9;

        // Step 2: Parse Database Section
        while (pointer < rdbBytes.length) {
            int opcode = Byte.toUnsignedInt(rdbBytes[pointer++]);

            if (opcode == 0xFF) { // End of File
                break;
            } else if (opcode == 0xFE) { // Database selector
                readLengthEncodedInt(); // Read and discard DB number
                continue;
            } else if (opcode == 0xFB) { // resizedb section
                readLengthEncodedInt(); // Read and discard main hash table size
                readLengthEncodedInt(); // Read and discard expiry hash table size
                continue;
            }

            // At this point, we are at the start of a key-value pair.
            // The 'opcode' we just read is actually the value-type.
            // We need to rewind one byte to pass it to the key-value parser.
            pointer--;
            parseKeyValuePair();
        }
    }

    private void parseKeyValuePair() {
        long expiryMillis = -1;
        int valueType;

        int opcode = Byte.toUnsignedInt(rdbBytes[pointer++]);

        if (opcode == 0xFD) { // Expiry in seconds
            expiryMillis = readLittleEndianLong(4) * 1000L;
            valueType = Byte.toUnsignedInt(rdbBytes[pointer++]);
        } else if (opcode == 0xFC) { // Expiry in milliseconds
            expiryMillis = readLittleEndianLong(8);
            valueType = Byte.toUnsignedInt(rdbBytes[pointer++]);
        } else {
            valueType = opcode;
        }

        // We only handle string encoding (type 0) for this stage
        if (valueType == 0) {
            String key = readStringEncoded();
            String value = readStringEncoded();
            KeyValueStore.getInstance().set(key, value, (int) expiryMillis);
        }
    }

    private int readLengthEncodedInt() {
        int firstByte = Byte.toUnsignedInt(rdbBytes[pointer++]);
        int type = (firstByte & 0xC0) >> 6; // Get the first 2 bits

        switch (type) {
            case 0: // 0b00 -> Next 6 bits are the length
                return firstByte & 0x3F;
            case 1: // 0b01 -> Next 14 bits are the length
                int secondByte = Byte.toUnsignedInt(rdbBytes[pointer++]);
                return ((firstByte & 0x3F) << 8) | secondByte;
            case 2: // 0b10 -> Next 4 bytes are the length
                return (int) readBigEndianLong(4);
            default: // 0b11 -> Special format, not needed for this stage
                return -1; // Should not happen for length
        }
    }

    private String readStringEncoded() {
        int length = readLengthEncodedInt();
        String value = new String(rdbBytes, pointer, length);
        pointer += length;
        return value;
    }

    private long readLittleEndianLong(int bytes) {
        long result = 0;
        for (int i = 0; i < bytes; i++) {
            result |= (long) (rdbBytes[pointer + i] & 0xFF) << (i * 8);
        }
        pointer += bytes;
        return result;
    }

    private long readBigEndianLong(int bytes) {
        long result = 0;
        for (int i = 0; i < bytes; i++) {
            result = (result << 8) | (rdbBytes[pointer + i] & 0xFF);
        }
        pointer += bytes;
        return result;
    }
}