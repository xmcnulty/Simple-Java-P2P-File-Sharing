package jtorrent.cmd;

import jtorrent.protocols.bittorrent.metainfo.Metainfo;

import java.io.File;
import java.io.IOException;

/**
 * Creates a torrent from a file
 * Created by Xavier on 5/2/17.
 */
public class CreateTorrent {
    public static void main(String[] args) throws IOException {
        Metainfo metainfo = Metainfo.createTorrentFromFile(new File(args[0]), args[1]);

        metainfo.writeToFile();
    }
}
