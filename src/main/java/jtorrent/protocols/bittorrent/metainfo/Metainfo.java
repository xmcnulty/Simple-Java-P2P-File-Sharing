package jtorrent.protocols.bittorrent.metainfo;

import com.google.gson.Gson;
import jtorrent.common.Utils;

import java.io.*;
import java.security.NoSuchAlgorithmException;
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

    private static final long serialVersionUID = 1L;

    public static final long CHUNK_SIZE_BYTES = 100000;

    // Keys for dictionary.
    public static final String ANNOUNCE_KEY = "announce",
        INFO_KEY = "info";

    private final Dictionary<String, Object> META_INFO;
    private final InfoDictionary INFO_DICTIONARY;
    private String JSON;

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

    public void setAnnounce(String ipPort) {
        if (META_INFO.get(ANNOUNCE_KEY) != null)
            META_INFO.remove(ANNOUNCE_KEY);

        META_INFO.put(ANNOUNCE_KEY, ipPort);

        JSON = (new Gson()).toJson(META_INFO);
    }

    /**
     * Returns the info of this metafile.
     * @return InfoDictionary object that contains the info of this file.
     */
    public InfoDictionary getInfo() {
        return INFO_DICTIONARY;
    }

    public String getJSON() {return JSON;}

    /**
     * Gets the name of the file for this torrent.
     * @return
     */
    public String getName() {
        return (String) getInfo().get().get(InfoDictionary.NAME_KEY);
    }

    /**
     * Writes this object to a file using the name info.name.
     * @return Name of the file if successful, empty string otherwise.
     */
    public String writeToFile() {
        String fileName = getName() + ".jtorrent";

        try (ObjectOutputStream write = new ObjectOutputStream(new FileOutputStream(fileName))) {
            write.writeObject(this);
            write.close();

            return fileName;
        } catch (Exception e) {
            return "";
        }
    }

    public void setTracker(String ip) {
        META_INFO.put(ANNOUNCE_KEY, ip);
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

    public static Metainfo createTorrentFromFile(File file, String announceIp) throws IOException {
        if (file == null || file.isDirectory())
            return null;

        long fileSize = file.length();

        long numChunks = (fileSize % CHUNK_SIZE_BYTES == 0) ?
                fileSize / CHUNK_SIZE_BYTES : (fileSize / CHUNK_SIZE_BYTES) + 1;

        byte [][] chunkHashes = new byte[(int) numChunks][];

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[(int) CHUNK_SIZE_BYTES];
        int read = 0;
        int count = 0;
        while ((read = fis.read(buffer)) > 0) {
            if (read < CHUNK_SIZE_BYTES) {
                byte[] buffer1 = new byte[read];

                for (int i=0; i < read; i++)
                    buffer1[i] = buffer[i];

                try {
                    chunkHashes[count] = Utils.hash(buffer1);
                } catch (NoSuchAlgorithmException e) {

                }
            } else {
                try {
                    chunkHashes[count] = Utils.hash(buffer);
                } catch (NoSuchAlgorithmException e) {
                }
            }

            count ++;
        }

        InfoDictionary infoDictionary = new InfoDictionary(fileSize, file.getName(), CHUNK_SIZE_BYTES, chunkHashes);
        Metainfo meta = new Metainfo(announceIp, infoDictionary);

        return meta;
    }
}
