package jtorrent.client;

import jtorrent.common.JPeer;
import jtorrent.common.JTorrent;
import jtorrent.common.Utils;

import java.io.File;
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

    private FileChunk chunkedFile;

    private JPeer self;

    private ClientConnectionHandler connectionHandler;

    private Client(InetAddress address, int port, JTorrent torrent) throws IOException {
        this.address = address;
        this.torrent = torrent;

        String id = Client.BITTORRENT_ID_PREFIX + UUID.randomUUID()
                .toString().split("-")[4];

        self = new JPeer(address.getHostAddress(), port, Utils.hexStringToByteArray(id));

        connectionHandler = new ClientConnectionHandler(this.torrent, this.address, port, id);
        connectionHandler.addListener(this);
    }

    /**
     * Creates a new leecher. A client that doesn't have a file and needs to download it.
     * @param address IP address of the client.
     * @param port Listening port of the client.
     * @param torrent Torrent
     * @return Client (leecher).
     * @throws IOException
     */
    public static Client newLeecher(InetAddress address, int port, JTorrent torrent) throws IOException {
        Client c = new Client(address, port, torrent);

        File f = new File(torrent.getName());

        c.chunkedFile = new FileChunk(torrent.getInfo(), f, false);

        return c;
    }

    /**
     * Creates a new seeder. A client that uploads file chunks.
     * @param address IP address
     * @param port Listening port
     * @param torrent Torrent
     * @param file File being shared (info held in torrent)
     * @return Seeder client
     */
    public static Client newSeeder(InetAddress address, int port, JTorrent torrent, File file) throws IOException {
        Client c = new Client(address, port, torrent);

        c.chunkedFile = new FileChunk(torrent.getInfo(), file, true);

        return c;
    }

    @Override
    public void handleNewPeerConnection(SocketChannel channel, byte[] peerId) {

    }

    @Override
    public void handleFailedConnection(JPeer peer) {

    }
}
