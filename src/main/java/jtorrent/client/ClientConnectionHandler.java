package jtorrent.client;

import jtorrent.common.JPeer;
import jtorrent.common.JTorrent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Xavier on 4/30/17.
 */
public class ClientConnectionHandler implements Runnable {
    private JTorrent torrent;
    private InetSocketAddress address;
    private String id;

    private ServerSocketChannel channel;
    private Set<PeerListener> listeners;

    private AtomicBoolean stop;

    private ExecutorService executorService;
    private Thread thread;


    public ClientConnectionHandler(JTorrent torrent, InetAddress address, int port, String id) throws IOException {
        this.torrent = torrent;
        this.id = id;

        // start the socket
        this.address = new InetSocketAddress(address, port);
        channel = ServerSocketChannel.open();
        channel.socket().bind(this.address);
        channel.configureBlocking(false);

        listeners = new HashSet<>();

        stop = new AtomicBoolean(false);
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
        stop.set(false);

        if (executorService == null || executorService.isShutdown()) {
            executorService = new ThreadPoolExecutor(10, 20,
                    10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        }

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            thread.setName("client-connection-handler");
            thread.start();
        }
    }

    public void addListener(PeerListener listener) {
        listeners.add(listener);
    }

    public void stop() {
        stop.set(true);

        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (executorService != null && !this.executorService.isShutdown()) {
            executorService.shutdown();
        }

        executorService = null;
        thread = null;
    }

    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
            channel = null;
        }
    }

    @Override
    public void run() {
        while (!stop.get()) {
            try {
                SocketChannel client = channel.accept();

                if (client != null)
                    accept(client);
            } catch (SocketTimeoutException se) {
                // ignore, there is no incoming connection
            } catch (IOException e) {
                e.printStackTrace();
                this.stop();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void accept(SocketChannel client) {
        // need to parse incoming message, ie send file or accept file chunk.
    }

    public interface PeerListener extends EventListener {
        public void handleNewPeerConnection(SocketChannel channel, byte[] peerId);

        public void handleFailedConnection(JPeer peer);
    }
}
