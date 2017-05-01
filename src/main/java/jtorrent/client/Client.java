package jtorrent.client;

import jtorrent.common.JPeer;
import jtorrent.common.JTorrent;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * Client that shares and downloads a torrent.
 *
 * Created by Xavier on 4/30/17.
 */
public class Client implements ClientConnectionHandler.PeerListener {
    private static final String BITTORRENT_ID_PREFIX = "-TO0042-";

    private InetAddress address;
    private JTorrent torrent;

    private JPeer self;

    private ClientConnectionHandler connectionHandler;

    public Client(InetAddress address, int port, JTorrent torrent) throws IOException {
        this.address = address;
        this.torrent = torrent;

        String id = Client.BITTORRENT_ID_PREFIX + UUID.randomUUID()
                .toString().split("-")[4];

        connectionHandler = new ClientConnectionHandler(this.torrent, this.address, port, id);
        connectionHandler.addListener(this);
    }

    @Override
    public void handleNewPeerConnection(SocketChannel channel, byte[] peerId) {

    }

    @Override
    public void handleFailedConnection(JPeer peer) {

    }
}
