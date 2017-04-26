package jtorrent.tracker;

import jtorrent.common.JPeer;
import jtorrent.common.JTorrent;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
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

    /**
     * Add a peer that will be using this torrent.
     * @param peer Peer that will use this torrent.
     */
    public void addPeer(JTrackedPeer peer) {
        peers.put(peer.getPeerIdHex(), peer);
    }

    /**
     * Retrieves a peer using this torrent by id.
     * @param id Hex string id of the peer.
     * @return Peer[id] or null if peer doesn't exist.
     */
    public JTrackedPeer getPeer(String id) {
        return peers.getOrDefault(id, null);
    }

    /**
     * Removes a peer from this swarm.
     * @param id Hex string id of the peer.
     * @return Peer that was removed, null if non-existent in the current swarm.
     */
    public JTrackedPeer removePeer(String id) {
        return peers.remove(id);
    }

    /**
     * Number of seeders on this torrent.
     * @return Number of seeders on the torrent.
     */
    public int numSeeders() {
        int seederCount = 0;
        for (JTrackedPeer p : peers.values())
            if (p.isCompleted())
                seederCount ++;

        return seederCount;
    }

    /**
     * Number of leechers on this torrent.
     * @return Number of leechers on this torrent.
     */
    public int numLeechers() {
        int leecherCount = 0;
        for (JTrackedPeer p : peers.values())
            if (!p.isCompleted())
                leecherCount ++;

        return leecherCount;
    }

    /**
     * Removes unfresh peers. These are peers that haven't announced within
     * the maximum allowed refresh time.
     */
    public void removeUnfreshPeers() {
        for (JTrackedPeer p : peers.values())
            if (!p.isFresh())
                peers.remove(p.getPeerIdHex());
    }

    // TODO: Need a method to update the state of the torrent swarm as individual peer states change.

    /**
     * Gets a list of peers than can be sent in a response message to an announce sent by a peer in the swarm.
     * @param peer Peer that sent the announce.
     * @return List of peers to send in the response. These peers are available for the
     */
    public Collection<JPeer> getAnnounceResponsePeers(JTrackedPeer peer) {
        Collection<JTrackedPeer> candidatePeers = peers.values();
        Collection<JPeer> responsePeers = new HashSet<>();

        int responsePeerCount = 0;

        for (JTrackedPeer p : candidatePeers) {
            // TODO: Fill this, build a list of valid peers to send in an announce response.
        }

        return responsePeers;
    }
}
