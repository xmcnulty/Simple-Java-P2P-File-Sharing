package jtorrent.common;

//import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import java.io.ByteArrayOutputStream;
import jtorrent.protocols.bittorrent.metainfo.InfoDictionary;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Dictionary;

/**
 * A torrent to be tracked by the network's Tracker.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/25/17.
 */
public class JTorrent {
    private final Metainfo metainfo;
    private final boolean seeder;

    private final byte[] info_hash;

    private final URL tracker;

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

        // need to create a hash of the source file's information
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(metainfo.getInfo());
        out.flush();

        byte[] encodedInfo = bos.toByteArray();
        info_hash = Utils.hash(encodedInfo);

        // attempt to get the tracker from the metainfo file.
        tracker = new URL("http://" + metainfo.getTracker());
    }

    public String infoHash() {
        return Utils.bytesToHex(info_hash);
    }

    public String getName() {return metainfo.getName();}

    public InfoDictionary getInfo() {return metainfo.getInfo();}

    public String getAddress() {
        return metainfo.getAnnounceAddress();
    }

    /**
     * Writes the metainfo to a file
     * @return Path the saved file.
     */
    public String save() {
        return metainfo.writeToFile();
    }
}
