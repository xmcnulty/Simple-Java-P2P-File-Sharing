package jtorrent.common;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A torrent to be tracked by the network's Tracker.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/25/17.
 */
public final class JTorrent {
    private final Metainfo metainfo;
    private final boolean seeder;

    private final byte[] encodedInfo; // Byte array of metainfo.info dictionary.
    private final byte[] info_hash;

    private final URI tracker;

    /**
     * Create a torrent from a meta-info file.
     * @param torrent Metainfo object representing a meta-info file.
     * @param seeder Whether this file will be seeded or not.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public JTorrent(Metainfo torrent, boolean seeder) throws NoSuchAlgorithmException, IOException, URISyntaxException {
        this.metainfo = torrent;
        this.seeder = seeder;

        ByteOutputStream bos = new ByteOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(metainfo.getInfo());
        out.flush();

        encodedInfo = bos.getBytes();
        info_hash = JTorrent.hash(encodedInfo);

        // attempt to get the tracker from the metainfo file.
        tracker = new URI(metainfo.getTracker());
    }

    /**
     * Creates a JTorrent object from a file containing a Metainfo object.
     * @param file Path to a .jmeta file.
     * @param seeder Whether this file will be seeded or not
     * @return JTorrent created from file.
     */
    public static JTorrent load(String file, boolean seeder) throws IOException,
            ClassNotFoundException, URISyntaxException, NoSuchAlgorithmException {
        return new JTorrent(new Metainfo(file), seeder);
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
     * Writes the metainfo to a file
     * @return Path the saved file.
     */
    public String save() {
        return metainfo.writeToFile();
    }
}
