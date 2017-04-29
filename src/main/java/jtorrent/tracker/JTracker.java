package jtorrent.tracker;

import jtorrent.common.JPeer;
import jtorrent.common.JTorrent;
import jtorrent.common.Utils;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;
import jtorrent.protocols.bittorrent.thp.AnnounceHandler;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracker that keeps track of torrent's and their swarm of peers.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/27/17.
 */
public class JTracker {
    private final Connection CONNECTION;
    private final InetSocketAddress ADDRESS;
    private final ConcurrentMap<String, TorrentRef> TORRENTS;

    private Thread announceThread; // thread used to handle
    private Thread peerCleanupThread; // periodically cleans up expired peers

    private final AtomicBoolean stopped;

    public JTracker(InetSocketAddress address) throws IOException {
        this.ADDRESS = address;
        TORRENTS = new ConcurrentHashMap<>();

        // create the connection. server
        CONNECTION = new SocketConnection(new ContainerSocketProcessor(new AnnounceHandler(TORRENTS)));

        stopped = new AtomicBoolean(false);

        peerCleanupThread = new Thread("clean-up-thread") {
            private final int REFRESH_TIME = 15; // refreshes every 15 seconds

            @Override
            public void run() {
                while (!stopped.get()) {
                    System.out.println(this.getName() + " cleaning up.");

                    for (TorrentRef torrent : TORRENTS.values())
                        torrent.removeExpired();

                    try {
                        Thread.sleep(REFRESH_TIME * 1000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        };
    }

    /**
     * Gets the full http url of this tracker server.
     * @return URL formatted as http://ip:port/announce
     */
    public final String url() {
        StringBuilder sb = new StringBuilder("http://").append(ADDRESS.getAddress().getCanonicalHostName())
                .append(":").append(ADDRESS.getPort()).append("/announce");

        return sb.toString();
    }

    /**
     * Starts running the tracker server.
     */
    public void start() {
        if (announceThread == null || !announceThread.isAlive()) {
            announceThread = new Thread(() -> {
                try {
                    System.out.println("Starting tracker at: " + url());
                    CONNECTION.connect(ADDRESS);
                } catch (IOException e) {
                    e.printStackTrace();
                    this.stop();
                }
            }, "tracker-announce-handler");

            announceThread.start();
        }

        if (!peerCleanupThread.isAlive()) {
            peerCleanupThread.start();
        }
    }

    /**
     * Stops the tracker server.
     */
    public void stop() {
        stopped.set(true);

        try {
            CONNECTION.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        announceThread.interrupt();
    }

    /**
     * Adds a torrent to this tracker.
     * @param torrent Torrent
     */
    public void addTorrent(Metainfo torrent) {
        try {
            TorrentRef torrentRef = new TorrentRef(torrent);
            TORRENTS.put(torrentRef.infoHash(), torrentRef);
            System.out.println("Added torrent " + torrent.getName());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a torrent from this tracker.
     * @param hash
     * @return
     */
    public TorrentRef removeTorrent(String hash) {
        return TORRENTS.remove(hash);
    }

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
