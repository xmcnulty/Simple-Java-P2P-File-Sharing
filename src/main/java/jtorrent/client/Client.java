package jtorrent.client;

import jtorrent.common.JPeer;
import jtorrent.common.JTorrent;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Client that shares and downloads a torrent.
 *
 * Created by Xavier on 4/30/17.
 */
public class Client {
    private static final String BITTORRENT_ID_PREFIX = "-TO0042-";

    private InetAddress address;
    private JTorrent torrent;

    private JPeer self;

    public Client(InetAddress address, int port, JTorrent torrent) {
        this.address = address;
        this.torrent = torrent;

        String id = Client.BITTORRENT_ID_PREFIX + UUID.randomUUID()
                .toString().split("-")[4];
    }
}
