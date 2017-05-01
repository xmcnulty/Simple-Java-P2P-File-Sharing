package jtorrent.client;

import jtorrent.common.JTorrent;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Xavier on 5/1/17.
 */
public class ClientTester {
    public static void main(String[] args) throws Exception {
        String filePath = "/Users/Xavier/agate.sh";
        String address = "172.20.20.20:4930";

        File file = new File(filePath);

        Metainfo metainfo = Metainfo.readFromFile("./agate.sh.jtorrent");
        JTorrent torrent = new JTorrent(metainfo, true);
        torrent.save();

        Client client = Client.newSeeder(InetAddress.getLocalHost(), 4934, torrent, file);

        client.start();

        System.in.read();
    }
}
