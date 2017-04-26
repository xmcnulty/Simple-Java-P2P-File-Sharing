package jtorrent.common;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Basic implementation of a java BitTorrent peer.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/25/17.
 */
public class JPeer {
    private final InetSocketAddress address;

    private ByteBuffer peerId;
    private String peerIdHex; // Hex string representation of peerId

    /**
     * Create a new Peer.
     * @param address InetSocketAddress for this peer.
     */
    public JPeer(InetSocketAddress address) {
        this(address, null);
    }

    /**
     * Create a new Peer.
     * @param address IP address for this peer.
     * @param port Port that this peer will listen for HTTP messages.
     */
    public JPeer(String address, int port) {
        this(new InetSocketAddress(address, port), null);
    }

    /**
     * Create a new Peer.
     * @param address IP address for this peer.
     * @param port Port that this peer will take incoming HTTP messages.
     * @param peerId ByteBuffer, unique identifier for this peer.
     */
    public JPeer(String address, int port, ByteBuffer peerId) {
        this(new InetSocketAddress(address, port), peerId);
    }

    /**
     * Create a new peer with address and peerId.
     * @param address InetSocketAddress for this peer.
     * @param peerId Unique ByteBuffer peer id.
     */
    public JPeer(InetSocketAddress address, ByteBuffer peerId) {
        this.address = address;

        setPeerId(peerId);
    }

    /**
     * Returns the host identifier of this peer.
     * @return String identifier of this peer (ip:port).
     */
    public final String getHostId() {
        return String.format("%d:%s", address.getAddress(), address.getPort());
    }

    public String getIp() {
        return address.getAddress().getHostAddress();
    }

    public int getPort() {
        return address.getPort();
    }

    public String getPeerIdHex() {
        return peerIdHex;
    }

    /**
     * Sets the peerId of this peer.
     * @param peerId ByteBuffer that is the id of this peer.
     */
    public void setPeerId(ByteBuffer peerId) {
        if (peerId == null) {
            this.peerId = null;
            this.peerIdHex = null;
        } else {
            this.peerId = peerId;
            this.peerIdHex = Utils.bytesToHex(this.peerId.array());
        }
    }

    /**
     * Dictionary of values that are used when sending a reply to an announce
     * from the tracker. This data contains the peer id, ip and port.
     * @return Dictionary containing id, ip and port
     */
    public Dictionary<String, Object> getReplyData() {
        Dictionary<String, Object> replyData = new Hashtable<>();

        if (peerId != null)
            replyData.put("peer id", peerId.array());

        replyData.put("ip", getIp());
        replyData.put("port", getPort());

        return replyData;
    }
}
