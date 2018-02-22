package com.gncompass.serverfront.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UuidHelper {
  public static final String LEADING_ZERO = "0x";

  /**
   * Returns the byte array from a string
   * @param uuid the string UUID to convert
   * @return the byte array of the UUID
   */
  public static byte[] getBytesFromUUID(String uuid) {
    return getBytesFromUUID(UUID.fromString(uuid));
  }

  /**
   * Returns the byte array from a UUID
   * @param uuid the UUID to convert
   * @return the byte array of the UUID
   */
  public static byte[] getBytesFromUUID(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());

    return bb.array();
  }

  /**
   * Returns the hex string from a UUID
   * @param uuid the UUID to convert
   * @param leadingZero TRUE to add '0x' portion to string
   * @return the string hex result
   */
  public static String getHexFromUUID(String uuid, boolean leadingZero) {
    return (leadingZero ? LEADING_ZERO : "") + uuid.replace("-", "");
  }

  /**
   * Returns the UUID from a byte array
   * @param bytes the byte array to convert
   * @return the UUID of the byte array
   */
  public static UUID getUUIDFromBytes(byte[] bytes) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    Long high = byteBuffer.getLong();
    Long low = byteBuffer.getLong();

    return new UUID(high, low);
  }
}
