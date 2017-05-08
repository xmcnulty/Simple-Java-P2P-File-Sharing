package jtorrent.cmd;

import jtorrent.client.Client;
import jtorrent.common.JTorrent;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

/**
 * Starts a new leecher client on the command line.
 * Created by Xavier on 5/1/17.
 */
public class RunLeecher {
    public static void main(String[] args) throws IOException, ClassNotFoundException, URISyntaxException, NoSuchAlgorithmException {
        int port = Integer.parseInt(args[0]);

        Metainfo metainfo = Metainfo.readFromFile(args[1]);
        String announce = args[2];

        metainfo.setAnnounce(announce);

        Client client = Client.newLeecher(Inet4Address.getLocalHost(), port, new JTorrent(metainfo, false));

        client.start();

        System.in.read();
    }
}
