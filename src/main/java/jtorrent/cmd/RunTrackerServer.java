package jtorrent.cmd;

import jtorrent.protocols.bittorrent.metainfo.Metainfo;
import jtorrent.tracker.JTracker;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;

/**
 * Runs a tracker server from the command line.
 * Created by Xavier on 5/1/17.
 */
public class RunTrackerServer {

    /**
     * Creates a tracker server that tracks one torrent.
     * @param args args[0] = port number for server args[1] = path to .jtorrent file.
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String ip = Inet4Address.getLocalHost().getHostAddress();
        int port = Integer.parseInt(args[0]);

        Metainfo metainfo = Metainfo.readFromFile(args[1]);

        JTracker server = new JTracker(new InetSocketAddress(ip, port));
        server.addTorrent(metainfo);

        server.start();

        System.in.read();
    }
}
