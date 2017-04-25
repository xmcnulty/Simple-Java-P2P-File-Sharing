package jtorrent.common;

/**
 * Class filled with static utility methods.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/25/17.
 */
public final class Utils {

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    /**
     * This class can't be instantiated
     */
    private Utils() {}

    /**
     * Converts an array of bytes to String hexadecimal format.
     * @param bytes Byte array.
     * @return Hex string representing bytes.
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}
