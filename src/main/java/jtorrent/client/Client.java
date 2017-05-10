package jtorrent.client;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import jtorrent.common.JPeer;
import jtorrent.common.JTorrent;
import jtorrent.common.Utils;
import jtorrent.protocols.bittorrent.thp.AnnounceHandlerRMI;
import jtorrent.tracker.JTracker;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client that shares and downloads a torrent.
 *
 * Created by Xavier on 4/30/17.
 */
public class Client {
    private static final String BITTORRENT_ID_PREFIX = "-TO0042-";

    private Connection connection;
    private final InetSocketAddress SOCKET_ADDRESS;

    private final InetAddress address;
    private final JTorrent torrent;
    private final int port;

    private ChunkedFile chunkedFile;

    private JPeer self;

    private Thread announceThread, seederThread, leecherThread;

    private AnnounceHandlerRMI announceHandlerRMI;

    private final AtomicBoolean stopped;

    private ArrayList<JPeer> seedingPeers;

    private Client(InetAddress address, int port, JTorrent torrent) throws IOException {
        this.address = address;
        this.torrent = torrent;
        this.port = port;

        SOCKET_ADDRESS = new InetSocketAddress(address.getHostAddress(), port);

        byte [] id = new byte[20];
        Random generator = new Random(System.nanoTime());
        generator.nextBytes(id);

        self = new JPeer(address.getHostAddress(), port, id);

        stopped = new AtomicBoolean(false);
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

        c.chunkedFile = new ChunkedFile(torrent.getInfo(), f, false);

        c.connection = new SocketConnection(new ContainerSocketProcessor(new SeederHandler(c.chunkedFile)));
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

        c.chunkedFile = new ChunkedFile(torrent.getInfo(), file, true);

        c.connection = new SocketConnection(new ContainerSocketProcessor(new SeederHandler(c.chunkedFile)));
        return c;
    }

    public void start() {
        setState(JPeer.State.STARTED);
        System.out.println("Starting client: " + self.getPeerId());

        if (announceThread == null || !announceThread.isAlive()) {
            try {
                announceThread = new Thread(new Announcer(), "announce-thread");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            announceThread.start();
        }

        if (chunkedFile.isSeeding() && (seederThread == null || !seederThread.isAlive())) {
            seederThread = new Thread(() -> {
                try {
                    connection.connect(SOCKET_ADDRESS);
                    System.out.println("Starting seeder listener on: " + SOCKET_ADDRESS.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, "seeder-thread");

            seederThread.start();
        } else if (!chunkedFile.isSeeding()) {
            leecherThread = new LeecherHandler(chunkedFile, this);

            leecherThread.start();
        }
    }

    public synchronized ArrayList<JPeer> getSeedingPeers() {
        return seedingPeers;
    }

    public synchronized void setSeedingPeers(ArrayList<JPeer> peers) {
        seedingPeers = peers;
    }

    public synchronized JPeer.State getState() {
        return self.getState();
    }

    public synchronized void setState(JPeer.State state) {
        self.setState(state);
    }

    /**
     * It is now okay to start seeding. Called by the leecher handler.
     */
    public void startSeeding() {
        System.out.println("Now seeding");
        if (chunkedFile.isSeeding() && (seederThread == null || !seederThread.isAlive())) {
            seederThread = new Thread(() -> {
                try {
                    connection.connect(SOCKET_ADDRESS);
                    System.out.println("Starting seeder listener on: " + SOCKET_ADDRESS.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, "seeder-thread");

            seederThread.start();
        }
    }
    
    public JTorrent getTorrent(){ return torrent; }

    /**
     * Sends period announce messages.
     */
    private class Announcer implements Runnable {
        private int announceIntervalSeconds = JTracker.TorrentRef.ANNOUNCE_INTERVAL_SECONDS;
        private final URL url;
        private final ConcurrentMap<String, Object> jsonValues;

        public Announcer() throws MalformedURLException {
            String addr = "http://" + torrent.getAddress() + "/announce";
            System.out.println("Announcing to: " + addr);
            url = new URL(addr);

            jsonValues = new ConcurrentHashMap<>();
            jsonValues.put("peer_id", self.getPeerId());
            jsonValues.put("info_hash", torrent.infoHash());
            jsonValues.put("ip", self.getIp());
            jsonValues.put("port", (Integer) self.getPort());
        }

        @Override
        public void run() {
            while (!stopped.get()) {
                try {
                    // add the state to the JSON values
                    jsonValues.put("event", self.getState().name());
                    jsonValues.put("left", chunkedFile.getLeft());
                    jsonValues.put("downloaded", chunkedFile.getWritten());
                    jsonValues.put("uploaded", 0);

                    String json = new Gson().toJson(jsonValues);

                    jsonValues.remove("event");
                    jsonValues.remove("left");
                    jsonValues.remove("downloaded");
                    jsonValues.remove("uploaded");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type",
                            "application/json");
                    connection.setRequestProperty("charset", "utf-8");
                    connection.setFixedLengthStreamingMode(json.length());
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);
                    connection.connect();

                    PrintStream ps = new PrintStream(connection.getOutputStream());
                    ps.print(json);
                    ps.flush();
                    ps.close();

                    connection.setConnectTimeout(10);
                    connection.setReadTimeout(10);
                    int respcode = connection.getResponseCode();

                    if (respcode == 200) {
                        Scanner in = new Scanner(connection.getInputStream());

                        String respBody = in.next();
                        System.out.println(respBody);
                        Map<String, Object> responseRoot= new Gson().fromJson(respBody, Map.class);

                        // update the seeding peers according to the tracker.
                        ArrayList<LinkedTreeMap<String, Object>> seeders =
                                (ArrayList<LinkedTreeMap<String, Object>>) responseRoot.get("peers");

                        ArrayList<JPeer> newSeeders = new ArrayList<>();

                        for (LinkedTreeMap<String, Object> d : seeders) {
                            String ip = (String) d.get("ip");
                            int port = ((Double) d.get("port")).intValue();
                            String id = (String) d.get("peer_id");

                            newSeeders.add(new JPeer(ip, port, Utils.hexStringToByteArray(id)));
                        }

                        setSeedingPeers(newSeeders);
                    }

                    Thread.sleep(announceIntervalSeconds * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Announcer that uses an RMI connection to a tracker server, rather than a socket connection.
     *
     * @author Xavier McNulty
     */
    private class AnnouncerRMI implements Runnable {
        @Override
        public void run() {

        }
    }
}
