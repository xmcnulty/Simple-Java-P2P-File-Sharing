package jtorrent.cmd;

import jtorrent.client.Client;
import jtorrent.common.JTorrent;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

/**
 * Runs a seeder client on the command line.
 * Created by Xavier on 5/1/17.
 */
public class RunSeeder {
    /**
     *
     * @param args args[0] = port number, args[1] = path to jtorrent, args[2] = path to source file
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, URISyntaxException, NoSuchAlgorithmException {
        int port = Integer.parseInt(args[0]);

        // Get metainfo
        Metainfo metainfo = Metainfo.readFromFile(args[1]);
        String announce = args[3];
        metainfo.setAnnounce(announce);

        Client seeder = Client.newSeeder(Inet4Address.getLocalHost(),
                port, new JTorrent(metainfo, true), new File(args[2]));

        seeder.start();

        System.in.read();
    }
}
