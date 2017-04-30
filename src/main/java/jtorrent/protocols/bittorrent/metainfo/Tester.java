package jtorrent.protocols.bittorrent.metainfo;

import jtorrent.common.Utils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Xavier on 4/24/17.
 */
public class Tester {
    public static void main(String args[]) {
        String filePath = "/Users/Xavier/Desktop/Addison.Wesley.Ethics.6th.Edition.Mar.2014.ISBN.0133741621.pdf";
        File f = new File(filePath);

        try {
            Metainfo metainfo = Metainfo.createTorrentFromFile(f, "1.1.1.1:90");
            System.out.println(metainfo.JSON);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
