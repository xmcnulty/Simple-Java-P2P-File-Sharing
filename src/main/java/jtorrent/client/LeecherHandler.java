package jtorrent.client;

import jtorrent.common.JPeer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Thread that requests file chunks from seeding peers on the network.
 * Uses an {@link Executor executor} to process incoming file chunks in parallel.
 *
 * @author Xavier McNulty
 * Created by Xavier on 5/1/17.
 */
public class LeecherHandler extends Thread {
    private final Executor executor;
    private final ChunkedFile file;
    private final Client client;
    private CountDownLatch latch;

    /**
     * Constructor.
     * @param file {@link ChunkedFile File} for the handler to write to.
     * @param client Owning {@link Client client} for this handler.
     */
    public LeecherHandler(ChunkedFile file, Client client) {
        super();
        this.file = file;
        this.client = client;

        executor = Executors.newFixedThreadPool(5);
    }

    /**
     * Periodically requests file chunks to be written to {@code file} from seeding peers.
     * Gets list of seeding peers on the network from the {@link Client Client} class.
     */
    @Override
    public void run() {
        Set<String> remainingChunks = file.neededChunks();

        while (!remainingChunks.isEmpty()) {
            ArrayList<JPeer> seeders = client.getSeedingPeers();

            if (seeders == null || seeders.isEmpty()) { // no available peers, sleep and wait
                System.out.println("No available seeders, waiting");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                continue;
            }

            latch = new CountDownLatch(seeders.size());


            // request a chunk from each peer.
            for (JPeer p : seeders) {
                for (String cHash : remainingChunks) {
                    executor.execute(new LeecherTask(p.getIp(), cHash, p.getPort()));

                    break;
                }
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            remainingChunks = file.neededChunks();
        }

        client.startSeeding();
    }

    /**
     * Private Runnable that makes a request to a seeding peer and writes the response
     * data to the file.
     */
    private class LeecherTask implements Runnable {
        String ip, chunkHash;
        int port;

        public LeecherTask(String ip, String chunkHash, int port) {
            this.ip = ip;
            this.chunkHash = chunkHash;
            this.port = port;
        }

        @Override
        public void run() {
            String address = "http://" + ip + ":" + port + "/" + chunkHash;
            try {
                URL url = new URL(address);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                connection.setConnectTimeout(100);

                connection.connect();

                if (connection.getResponseCode() == 200) {
                    chunkHash = connection.getResponseMessage();

                    InputStream is = connection.getInputStream();

                    file.writeChunk(chunkHash, is);

                    is.close();

                    System.out.println("read bytes");
                }
            } catch (SocketTimeoutException se) {
                System.out.println("Failed to connect");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
                return;
            }
        }
    }
}
