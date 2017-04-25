package jtorrent.protocols.bittorrent.metainfo;

import com.google.gson.Gson;

import java.io.*;
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
public final class Metainfo implements Serializable {
    // Keys for dictionary.
    public static final String ANNOUNCE_KEY = "announce",
        INFO_KEY = "info";

    private final Dictionary<String, Object> META_INFO;
    private final InfoDictionary INFO_DICTIONARY;
    public final String JSON;

    /**
     * Creates Metainfo for a torrent, only containing the fields required by BTP/1.0.
     * @param announce String URL of the tracker.
     * @param info Dictionary containing information for the file download.
     */
    public Metainfo(String announce, InfoDictionary info) {
        META_INFO = new Hashtable<>();

        INFO_DICTIONARY = info;

        META_INFO.put(ANNOUNCE_KEY, announce);
        META_INFO.put(INFO_KEY, INFO_DICTIONARY.get());

        JSON = (new Gson()).toJson(META_INFO);
    }

    /**
     * Attempts to construct a new Metainfo object from one written to a file.
     * @param path Path to file containing a written Metainfo object.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Metainfo(String path) throws IOException, ClassNotFoundException {
        Metainfo fromFile = readFromFile(path);

        META_INFO = fromFile.META_INFO;
        JSON = fromFile.JSON;
        INFO_DICTIONARY = fromFile.INFO_DICTIONARY;
    }

    /**
     * Returns the info of this metafile.
     * @return InfoDictionary object that contains the info of this file.
     */
    public InfoDictionary getInfo() {
        return INFO_DICTIONARY;
    }

    /**
     * Writes this object to a file using the name info.name.
     * @return Name of the file if successful, empty string otherwise.
     */
    public String writeToFile() {
        Dictionary<String, Object> info = (Dictionary<String, Object>) META_INFO.get(INFO_KEY);
        String fileName = (String) info.get(InfoDictionary.NAME_KEY) + ".jtorrent";

        try (ObjectOutputStream write = new ObjectOutputStream(new FileOutputStream(fileName))) {
            write.writeObject(this);
            write.close();

            return fileName;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Retrieves the tracker for this torrent. Held in the dictionary under
     * the announce key.
     * @return String URL of the tracker.
     */
    public String getTracker() {
        return (String) META_INFO.get(ANNOUNCE_KEY);
    }

    /**
     * Reads a serialized Metainfo object from a file.
     * @param path Path to the file.
     * @return Metainfo object obtained from file.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Metainfo readFromFile(String path) throws IOException, ClassNotFoundException {
        Object in;

        ObjectInputStream read = new ObjectInputStream(new FileInputStream(path));

        in = read.readObject();

        return (Metainfo) in;
    }
}
