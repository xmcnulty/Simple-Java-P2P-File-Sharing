package protocols.bittorrent.metainfo;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Dictionary used in a Metainfo file. This dictionary contains
 * only the required information for this dictionary, as defined in
 * the BTP/1.0 standard.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/24/17.
 */
final class InfoDictionary {
    // KEY-VALUES
    public static final String LENGTH_KEY = "length",
        NAME_KEY = "name", PIECE_LENGTH_KEY = "piece length",
        PIECES_KEY = "pieces";

    private final Dictionary<String, Object> INFO;

    /**
     * Creates a new Info dictionary, holding the required key-values specified in the BTP/1.0 standard.
     * @param length Length of the file in bytes.
     * @param name String containing the name of the file.
     * @param piece_length An integer indicating the number of bytes in each piece.
     * @param pieces An String array containing the 20-byte SHA1 hash values for all the pieces
     *               of the torrent. The hash of the first piece is at index 0.
     */
    public InfoDictionary(Integer length,
                          String name,
                          Integer piece_length,
                          String[] pieces) {
        INFO = new Hashtable<>();

        INFO.put(LENGTH_KEY, length);
        INFO.put(NAME_KEY, name);
        INFO.put(PIECE_LENGTH_KEY, piece_length);
        INFO.put(PIECES_KEY, pieces);
    }

    public Dictionary<String, Object> get() {
        return INFO;
    }
}
