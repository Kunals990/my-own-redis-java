package parser;

import store.KeyValueStore;

public class RDBParser {
    private final byte[] rdbBytes;
    private int pointer = 0;

    public RDBParser(byte[] rdbBytes) {
        this.rdbBytes = rdbBytes;
    }

    public void parse() {
        // Step 1: Parse Header
        // "REDIS" + 4-digit version number. We just skip it.
        pointer += 9; // Skip "REDIS0011"

        // For this stage, we can skip the metadata section.
        // A real parser would loop through 0xFA opcodes here.

        // Step 2: Parse Database Section
        int opcode = Byte.toUnsignedInt(rdbBytes[pointer++]);
        if (opcode != 0xFE) {
            // Expected database selector
            return;
        }

        // Read DB number, we can ignore it for now.
        readLengthEncodedInt();

        opcode = Byte.toUnsignedInt(rdbBytes[pointer++]);
        if (opcode != 0xFB) {
            // Expected resizedb section
            return;
        }

        // Read hash table sizes, we can ignore them.
        readLengthEncodedInt(); // main hash table size
        readLengthEncodedInt(); // expiry hash table size

        // Step 3: Loop through Key-Value pairs
        long expiryMillis = -1;
        while (pointer < rdbBytes.length) {
            opcode = Byte.toUnsignedInt(rdbBytes[pointer++]);

            if (opcode == 0xFF) { // End of File
                break;
            }
            if (opcode == 0xFE) { // Another DB selector, end of current DB
                pointer--; // Let the outer logic handle the next DB
                break;
            }

            if (opcode == 0xFC) { // Expiry in milliseconds
                // Read 8-byte little-endian long
                expiryMillis = readLittleEndianLong(8);
                // After expiry, the value type follows
                opcode = Byte.toUnsignedInt(rdbBytes[pointer++]);
            } else if (opcode == 0xFD) { // Expiry in seconds
                // Read 4-byte little-endian long, convert to ms
                expiryMillis = readLittleEndianLong(4) * 1000;
                // After expiry, the value type follows
                opcode = Byte.toUnsignedInt(rdbBytes[pointer++]);
            }

            // At this point, 'opcode' must be the value type.
            // For this stage, we only handle string values (type 0).
            if (opcode == 0) {
                String key = readStringEncoded();
                String value = readStringEncoded();
                KeyValueStore.getInstance().set(key, value, (int) expiryMillis);
                expiryMillis = -1; // Reset expiry for the next key
            }
        }
    }

    // Helper to read a length-encoded integer from the byte array
    private int readLengthEncodedInt() {
        // For this stage, we can assume the simple 1-byte length encoding (first 2 bits are 00)
        // A full implementation would check the first 2 bits.
        return Byte.toUnsignedInt(rdbBytes[pointer++]);
    }

    // Helper to read a string
    private String readStringEncoded() {
        int length = readLengthEncodedInt();
        String value = new String(rdbBytes, pointer, length);
        pointer += length;
        return value;
    }

    // Helper to read little-endian long
    private long readLittleEndianLong(int bytes) {
        long result = 0;
        for (int i = 0; i < bytes; i++) {
            result |= (long) (rdbBytes[pointer + i] & 0xFF) << (i * 8);
        }
        pointer += bytes;
        return result;
    }
}