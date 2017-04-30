package jtorrent.client;

import jtorrent.common.JTorrent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Xavier on 4/30/17.
 */
public class ClientConnectionHandler implements Runnable {
    private JTorrent torrent;
    private InetSocketAddress address;
    private String id;

    private ServerSocketChannel channel;
    private Set<PeerListener> listeners;


    public ClientConnectionHandler(JTorrent torrent, InetAddress address, int port, String id) throws IOException {
        this.torrent = torrent;
        this.id = id;

        // start the socket
        this.address = new InetSocketAddress(address, port);
        channel = ServerSocketChannel.open();
        channel.socket().bind(this.address);
        channel.configureBlocking(false);

        listeners = new HashSet<>();
    }

    /**
     * Registers a new listener with this.
     * @param listener
     */
    public void register(PeerListener listener) {
        listeners.add(listener);
    }

    /**
     * Starts this
     */
    public void start() {

    }

    @Override
    public void run() {

    }

    private class PeerListener implements EventListener {

    }
}
