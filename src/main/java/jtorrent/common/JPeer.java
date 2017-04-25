package jtorrent.common;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

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
}
