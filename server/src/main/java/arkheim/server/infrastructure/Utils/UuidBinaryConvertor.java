package arkheim.server.infrastructure.Utils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UuidBinaryConvertor {
    /**
     * Converts UUID to 16 bit Binary, used for conversion of UUID class to BINARY(16) data type in MySQL DB
     * @param uuid UUID That is going to get converted
     * */
    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    /**
     * Converts 16 bit Binary to UUID, used for conversion of BINARY(16) data type in MySQL DB to UUID class.
     * @param bytes Raw data That is going to get converted
     * */
    public static UUID bytesToUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }
}
