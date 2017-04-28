package jtorrent.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    /**
     * Gets the SHA-1 hash of metainfo.info.
     * @param data Byte array of serialized InfoDictionary from metainfo.
     * @return SHA-1 hash of data.
     * @throws NoSuchAlgorithmException
     */
    public static byte[] hash(byte [] data) throws NoSuchAlgorithmException {
        MessageDigest crypt;
        crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(data);

        return crypt.digest();
    }

    /**
     * Converts a hex string to a byte array.
     * @param s Hex string.
     * @return byte array derived from s.
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
