package jtorrent.tracker;

import jtorrent.common.JTorrent;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracked torrents are tracked by the network tracker and are not expected
 * to have data files associated with them.
 *
 * These are used to represent torrents announced by the tracker.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/25/17.
 */
public class JTrackedTorrent extends JTorrent {

    public static final int MAX_ANNOUNCE_INTERVAL_SECONDS = 5;
    private static final int DEFAULT_ANSWER_NUM_PEERS = 30;
    private static final int DEFAULT_ANNOUNCE_INTERVAL_SECONDS = 10;

    private int answerPeers, announceInterval;

    private ConcurrentMap<String, JTrackedPeer> peers;

    /**
     * Creates a new tracked torrent.
     * @param torrent Metainfo object that is the torrent.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws URISyntaxException
     */
    public JTrackedTorrent(Metainfo torrent) throws NoSuchAlgorithmException, IOException, URISyntaxException {
        super(torrent, false);

        answerPeers = DEFAULT_ANSWER_NUM_PEERS;
        announceInterval = DEFAULT_ANNOUNCE_INTERVAL_SECONDS;
        peers = new ConcurrentHashMap<>();
    }
}
