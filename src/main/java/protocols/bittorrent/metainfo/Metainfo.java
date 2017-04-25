package protocols.bittorrent.metainfo;

import com.google.gson.Gson;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * This class represents a Metainfo file, only containing the required key-value
 * pairs, as specified in the BTP/1.0 standard.
 *
 * For the simplicity of the assignment, Metainfo files will only contain one torrent.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/24/17.
 */
final class Metainfo {
    // Keys for dictionary.
    public static final String ANNOUNCE_KEY = "announce",
        INFO_KEY = "info";

    private final Dictionary<String, Object> META_INFO;
    public final String JSON;

    /**
     * Creates Metainfo for a torrent, only containing the fields required by BTP/1.0.
     * @param announce String URL of the tracker.
     * @param info Dictionary containing information for the file download.
     */
    public Metainfo(String announce, InfoDictionary info) {
        META_INFO = new Hashtable<>();

        META_INFO.put(ANNOUNCE_KEY, announce);
        META_INFO.put(INFO_KEY, info.get());

        JSON = (new Gson()).toJson(META_INFO);
    }
}
