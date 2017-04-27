package jtorrent.common;

import java.net.InetSocketAddress;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Basic implementation of a java BitTorrent peer.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/25/17.
 */
public class JPeer {

    /**
     * Possible states for a peer exchanging on this torrent.
     * STARTED is the state when a Peer has announced itself and is about
     *  to start exchanching data with other peers.
     * COMPLETED is the state of when a peer when it has finished downloading the file.
     * STOPPED  is the state when a is the state right before a peer is removed from a swarm.
     */
    protected enum State {
        NONE,
        STARTED,
        COMPLETED,
        STOPPED
    }

    private final InetSocketAddress address;

    private String peerId;
    protected State state; // current state of this peer.

    public JPeer(String ip, int port, byte[] id) {
        if (id == null || id.length != 20)
            throw new IllegalArgumentException("Peer id must be 20 bytes.");

        address = new InetSocketAddress(ip, port);
        peerId = Utils.bytesToHex(id);
    }

    /**
     * String representation of this peer.
     * @return String of this peer in the format
     *  Peer [%id] at %ip:%port.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append("Peer [").append(peerId)
                .append("] at ").append(getIp()).append(":").append(getPort());

        return builder.toString();
    }

    public String getIp() {
        return address.getAddress().getHostAddress();
    }

    public int getPort() {
        return address.getPort();
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(byte[] id) {
        if (id == null || id.length != 20)
            throw new IllegalArgumentException("Peer id must be 20 bytes.");

        peerId = Utils.bytesToHex(id);
    }

    /**
     * Dictionary of values that are used when sending a reply to an announce
     * from the tracker. This data contains the peer id, ip and port.
     * @return Dictionary containing id, ip and port
     */
    public Dictionary<String, Object> getReplyData() {
        Dictionary<String, Object> replyData = new Hashtable<>();

        if (peerId != null)
            replyData.put("peer id", peerId);

        replyData.put("ip", getIp());
        replyData.put("port", getPort());

        return replyData;
    }

    /**
     * Compares the equality of JPeers based on peer id.
     * @param obj JPeer to compare to this.
     * @return true if equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof JPeer))
            return false;

        JPeer p = (JPeer) obj;

        return peerId == p.peerId;
    }
}
