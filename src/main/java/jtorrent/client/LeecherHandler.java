package jtorrent.client;

import jtorrent.common.JPeer;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;

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
 * Requests file chunks from seeding peers.
 * Created by Xavier on 5/1/17.
 */
public class LeecherHandler extends Thread {
    private final Executor executor;
    private final ChunkedFile file;
    private final Client client;
    private CountDownLatch latch;

    public LeecherHandler(ChunkedFile file, Client client) {
        super();
        this.file = file;
        this.client = client;

        executor = Executors.newFixedThreadPool(5);
    }

    @Override
    public void run() {
        Set<String> remainingChunks = file.neededChunks();

        while (!remainingChunks.isEmpty()) {
            ArrayList<JPeer> seeders = client.getSeedingPeers();

            if (seeders.isEmpty()) { // no available peers, sleep and wait
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
        }

        client.startSeeding();
    }

    private class LeecherTask implements Runnable {
        String ip, chunkHash;
        int port;
        CountDownLatch latch;

        public LeecherTask(String ip, String chunkHash, int port) {
            this.ip = ip;
            this.chunkHash = chunkHash;
            this.port = port;
            this.latch = latch;
        }

        @Override
        public void run() {
            String address = "http://" + ip + ":" + port + "/" + chunkHash;
            try {
                URL url = new URL(address);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                connection.setConnectTimeout(5000);

                connection.connect();

                if (connection.getResponseCode() == 200) {
                    chunkHash = connection.getResponseMessage();

                    InputStream is = connection.getInputStream();
                    byte[] bytes = new byte[(int) Metainfo.CHUNK_SIZE_BYTES];
                    is.close();

                    int read = is.read(bytes);

                    if (read < Metainfo.CHUNK_SIZE_BYTES) {
                        byte[] b = bytes;
                        bytes = new byte[read];

                        for (int i = 0; i < read; i++)
                            bytes[i] = b[i];
                    }

                    file.writeChunk(chunkHash, bytes);
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
