package jtorrent.tracker;

import jtorrent.common.JPeer;
import jtorrent.common.JTorrent;
import jtorrent.common.Utils;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Tracker that keeps track of torrent's and their swarm of peers.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/27/17.
 */
public class JTracker {

    /**
     * A reference to a peer that this tracker is following.
     */
    public class PeerRef extends JPeer {
        // if a peer does not announce itself in this amount of time it will not be used by the tracker.
        private static final int EXPIRE_TIME_SECONDS = 15;
        private Date lastAnnounceTime = null; // set when an announce from this peer is seen

        JTorrent torrent; // torrent that this peer is following

        private long bytesUploaded = 0, // number of bytes to uploaded by this peer.
                bytesDowloaded = 0, // number of bytes downloaded by this peer.
                bytesLeft = 0; // number of bytes left for this peer to downnload.

        /**
         * Creates a new peer reference for this tracker to follow.
         * @param torrent Torrent that the peer is using.
         * @param ip IP address of the peer.
         * @param port Port of the peer.
         * @param id Unique peer id
         */
        public PeerRef(JTorrent torrent, String ip, int port, byte[] id) {
            super(ip, port, id);

            this.torrent = torrent;
            state = State.NONE;
        }

        /**
         * Checks if this peer has expired or not.
         * @return true if the peer is expired.
         */
        public boolean isExpired() {
            if (lastAnnounceTime == null)
                return false; // peer hasn't even started yet

            return lastAnnounceTime.getTime() + (1000 * EXPIRE_TIME_SECONDS) > (new Date()).getTime();
        }

        /**
         * Updates the state of this peer reference when the tracker receives an announce for this peer.
         * @param newState New state of the peer.
         * @param bytesUploaded New number of bytes uploaded by this peer.
         * @param bytesDowloaded New number of bytes downloaded.
         * @param bytesLeft New number of bytes left.
         */
        public void update(State newState, long bytesUploaded, long bytesDowloaded, long bytesLeft) {
            state = (bytesLeft == 0 && newState == State.STARTED) ? State.COMPLETED : newState;
            this.bytesDowloaded = bytesDowloaded;
            this.bytesUploaded = bytesUploaded;
            this.bytesLeft = bytesLeft;
        }
    }

    /**
     * A tracker's reference to a torrent. This reference will contain a list of
     * peers that are actively using this torrent.
     */
    public class TorrentRef extends JTorrent {
        private final ConcurrentMap<String,PeerRef> PEERS; // Peers actively using this torrent
        public static final int ANNOUNCE_INTERVAL_SECONDS = 5; // announce interval

        /**
         * Creates a new torrent reference to be used by a tracker.
         * @param torrent Torrent that this is referencing.
         * @throws NoSuchAlgorithmException
         * @throws IOException
         * @throws URISyntaxException
         */
        public TorrentRef(Metainfo torrent) throws NoSuchAlgorithmException,
                IOException, URISyntaxException {
            super(torrent, false);

            PEERS = new ConcurrentHashMap<>();
        }

        // add a peer to this torrent
        public PeerRef addPeer(PeerRef peer) {
            return PEERS.put(peer.getPeerId(), peer);
        }

        // Get a peer by id.
        public PeerRef getPeer(String id) {
            return PEERS.get(id);
        }

        // remove a peer from from this torrent
        public PeerRef removePeer(String id) {
            return PEERS.remove(id);
        }

        // Remove all expired peers from this torrent.
        public void removeExpired() {
            for (PeerRef p : PEERS.values())
                if (p.isExpired())
                    PEERS.remove(p.getPeerId());
        }

        /**
         * This is called when a peer sends the tracker a started message, effectively starting
         * that peer's interaction with this torrent.
         * @param ip IP address of the peer.
         * @param port Port of the peer.
         * @param id Peer's id.
         * @param uploaded Bytes uploaded by peer.
         * @param downloaded Bytes downloaded by peer.
         * @param left Bytes left to download by peer.
         * @return New tracker reference to the peer.
         */
        public PeerRef peerStarted(String ip, int port, String id, long uploaded, long downloaded, long left) {
            PeerRef p = new PeerRef(this, ip, port, Utils.hexStringToByteArray(id));
            p.update(JPeer.State.STARTED, uploaded, downloaded, left);

            return addPeer(p);
        }

        /**
         * This is called when a peer sends the tracker a stopped message, ceasing that peer's
         * use of this torrent.
         * @param id Peer's id.
         * @param uploaded Bytes uploaded by peer.
         * @param downloaded Bytes downloaded by peer.
         * @param left Bytes left to download by peer.
         * @return New tracker reference to the peer.
         */
        public PeerRef peerStopped(String id, long uploaded, long downloaded, long left) {
            PeerRef p = removePeer(id);
            p.update(JPeer.State.STOPPED, uploaded, downloaded, left);

            return p;
        }

        /**
         * Called when a peer sends a completed announce to the tracker
         * @param id Peer's id.
         * @param uploaded Bytes uploaded by peer.
         * @param downloaded Bytes downloaded by peer.
         * @param left Bytes left to download by peer.
         */
        public PeerRef peerCompleted(String id, long uploaded, long downloaded, long left) {
            PeerRef p = getPeer(id);
            p.update(JPeer.State.COMPLETED, uploaded, downloaded, left);

            return p;
        }

        /**
         * Default behavior of an announce with an empty action state. Updates the
         * peer and sets its state to started
         * @param id Peer's id.
         * @param uploaded Bytes uploaded by peer.
         * @param downloaded Bytes downloaded by peer.
         * @param left Bytes left to download by peer.
         */
        public PeerRef peerDefaultAnnounce(String id, long uploaded, long downloaded, long left) {
            PeerRef p = getPeer(id);
            p.update(JPeer.State.STARTED, uploaded, downloaded, left);

            return p;
        }

        /**
         * Returns a list of unexpired seeders on this torrent. Used to send a response to a
         * given peer.
         *
         * @return Unexpired seeders on this torrent.
         */
        public Collection<JPeer> getValidPeers(PeerRef requestingPeer) {
            ArrayList<JPeer> validPeers = new ArrayList<>();

            for (PeerRef p : PEERS.values()) {
                if (p.equals(requestingPeer)) // don't return this peer
                    continue;

                if (p.isExpired()) {// peer is expired and should be removed.
                    removePeer(p.getPeerId());
                    continue;
                }

                if (p.bytesLeft == 0) // peer needs to have all of the file
                    validPeers.add(p);
            }

            return validPeers;
        }
    }
}
