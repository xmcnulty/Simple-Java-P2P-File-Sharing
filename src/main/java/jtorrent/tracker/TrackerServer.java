package jtorrent.tracker;

import jtorrent.protocols.bittorrent.metainfo.Metainfo;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Executable class to run a tracker server.
 *
 * @author Xavier
 * Created by Xavier on 4/29/17.
 */
public class TrackerServer {
    /**
     * Main method.
     * @param args arg0 = port arg... List of .torrent files (metainfo) to preload to this tracker.
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String ip = Inet4Address.getLocalHost().getHostAddress();
        int port = Integer.parseInt(args[0]);

        JTracker tracker = new JTracker(new InetSocketAddress(ip, port));

        // add all desired pre-existing torrents
        for (int i=1; i < args.length; i++) {
            Metainfo torrent = Metainfo.readFromFile(args[i]);

            tracker.addTorrent(torrent);
        }

        tracker.start();
    }
}
