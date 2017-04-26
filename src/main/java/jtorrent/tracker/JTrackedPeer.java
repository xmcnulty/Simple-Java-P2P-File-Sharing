package jtorrent.tracker;

import jtorrent.common.JPeer;
import jtorrent.common.JTorrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Tracker peer that represents a peer exchanging on a given torrent.
 * This implementation only cares about the Peer states when they start and finish.
 *
 * This will also not expire peers unless they send a STOPPED announce request.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/25/17.
 */
public class JTrackedPeer extends JPeer {

    /**
     * Possible states for a peer exchanging on this torrent.
     * STARTED is the state when a Peer has announced itself and is about
     *  to start exchanching data with other peers.
     * COMPLETED is the state of when a peer when it has finished downloading the file.
     * STOPPED  is the state when a is the state right before a peer is removed from a swarm.
     */
    public enum JPeerState {
        UKNOWN,
        STARTED,
        COMPLETED,
        STOPPED
    }

    private static final Logger logger = LoggerFactory.getLogger(JTrackedPeer.class);

    private static final int REFRESH_TIME_SECONDS = 30;

    private long uploaded, downloaded, left;
    private JTorrent torrent;
    private JPeerState state;
    private Date lastAnnounce;

    /**
     * Create a new tracked peer for the given torrent.
     * @param torrent The torrent this peer exchanges on.
     * @param address IP address of the peer.
     * @param port The peer's port.
     * @param peerId Byte encoded peer id.
     */
    public JTrackedPeer(JTorrent torrent, String address, int port, ByteBuffer peerId) {
        super(address, port, peerId);

        this.torrent = torrent;
        this.state = JPeerState.UKNOWN;
        this.lastAnnounce = null;

        this.uploaded = 0;
        this.downloaded = 0;
        this.left = 0;
    }

    /**
     * Updates the peer's state and information. If the peer has 0 bytes left to download
     * the state will automatically be set to COMPLETED.
     * @param state The peer's state.
     * @param uploaded Uploaded byte count.
     * @param downloaded Downloaded byte count.
     * @param left Number of bytes left to download.
     */
    public void update(JPeerState state, long uploaded, long downloaded, long left) {
        if (JPeerState.STARTED.equals(state) && left == 0) {
            state = JPeerState.COMPLETED;
        }

        this.state = state;
        this.lastAnnounce = new Date();
        this.uploaded = uploaded;
        this.downloaded = downloaded;
        this.left = left;
    }

    /**
     * Checks if a peer has announced itself to the tracker within the maximum refresh time.
     * @return true if fresh
     */
    public boolean isFresh() {
        if (lastAnnounce == null)
            return false;

        return lastAnnounce.getTime() + (REFRESH_TIME_SECONDS * 1000) > new Date().getTime();
    }

    /**
     * Tells if this peer is finished downloading and can become a seeder.
     * @return True if download is finished.
     */
    public boolean isCompleted() {
        return JPeerState.COMPLETED.equals(state);
    }

    public long getUploaded() {
        return uploaded;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public long getLeft() {
        return left;
    }
}
