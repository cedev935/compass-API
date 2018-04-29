package com.gncompass.serverfront.util;

public class StringHelper {
  private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
  private static final String REGEX_END = "$";
  private static final String REGEX_START = "^";
  private static final String REGEX_EMAIL = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
  private static final String REGEX_MIME = "[-\\w]+\\/[-\\w]+(\\.[-\\w]+)*([+][-\\w]+)?";
  private static final String REGEX_UUID = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}";
  private static final String REGEX_ACCESS_KEY = REGEX_START + REGEX_UUID + "-" + REGEX_UUID + REGEX_END;
  private static final String REGEX_EMAIL_ONLY = REGEX_START + REGEX_EMAIL + REGEX_END;
  private static final String REGEX_MIME_ONLY = REGEX_START + REGEX_MIME + REGEX_END;
  private static final String REGEX_UUID_ONLY = REGEX_START + REGEX_UUID + REGEX_END;

  private static final int UUID_LENGTH = 36;

  /**
   * Standard bytes to hex functionality
   * @param bytes the bytes to convert to hex
   * @return the string hex value of the bytes provided (no 0x at start)
   */
  public static String bytesToHex(byte[] bytes) {
      char[] hexChars = new char[bytes.length * 2];
      for ( int j = 0; j < bytes.length; j++ ) {
          int v = bytes[j] & 0xFF;
          hexChars[j * 2] = HEX_ARRAY[v >>> 4];
          hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
      }
      return new String(hexChars);
  }

  /**
   * Returns the access key object, if the string is a proper access key
   * @param accessKey the access key to validate and divide in string form (from headers)
   * @return the access key divided object which contains the device id and the session key.
   *         NULL if not in proper form
   */
  public static AccessKey getAccessKey(String accessKey) {
    if(isAccessKey(accessKey)) {
      return new AccessKey(accessKey.substring(0, UUID_LENGTH),
                            accessKey.substring(UUID_LENGTH + 1));
    }
    return null;
  }

  /**
   * Returns if the string to check exactly matches the access key format
   * @param check the string to check if its an access key
   * @return TRUE if it matches. FALSE otherwise
   */
  public static boolean isAccessKey(String check) {
    return check.matches(REGEX_ACCESS_KEY);
  }

  /**
   * Returns if the string to check exactly matches the email format
   * @param check the string to check if its email
   * @return TRUE if it matches. FALSE otherwise
   */
  public static boolean isEmail(String check) {
    return check.matches(REGEX_EMAIL_ONLY);
  }

  /**
   * Returns if the string to check exactly matches the mime format
   * @param check the string to check if its mime
   * @return TRUE if it matches. FALSE otherwise
   */
  public static boolean isMime(String check) {
    return check.matches(REGEX_MIME_ONLY);
  }

  /**
   * Returns if the string to check exactly matches the UUID format
   * @param check the string to check if its UUID
   * @return TRUE if it matches. FALSE otherwise
   */
  public static boolean isUuid(String check) {
    return check.matches(REGEX_UUID_ONLY);
  }


  /**
   * Inner class for handling the access key string information in its proper format
   */
  public static class AccessKey {
    public String deviceId;
    public String sessionKey;

    public AccessKey(String deviceId, String sessionKey) {
      this.deviceId = deviceId;
      this.sessionKey = sessionKey;
    }
  }
}
