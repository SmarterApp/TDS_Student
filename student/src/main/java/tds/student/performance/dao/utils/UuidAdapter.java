package tds.student.performance.dao.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by jjohnson on 12/23/15.
 */
public class UuidAdapter {
    public static byte[] getBytesFromUuidString(String uuidString) {
        return getBytesFromUUID(UUID.fromString(uuidString));
    }

    // TODO:  Is this method even required?  Will we ever have an actual UUID that we need converted to a byte array?
    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    public static UUID getUuidFromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Long high = byteBuffer.getLong();
        Long low = byteBuffer.getLong();

        return new UUID(high, low);
    }
}
