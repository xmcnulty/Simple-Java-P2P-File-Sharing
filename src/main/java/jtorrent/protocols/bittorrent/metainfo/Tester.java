package jtorrent.protocols.bittorrent.metainfo;

import java.io.IOException;

/**
 * Created by Xavier on 4/24/17.
 */
public class Tester {
    public static void main(String args[]) {
        byte[][] hashes = {{(byte) 0xfe}};

        InfoDictionary iDict = new InfoDictionary(100,
                "test-file",
                50,
                hashes);

        Metainfo m = new Metainfo("http://test.com", iDict);

        System.out.println(m.JSON);

        System.out.println(m.writeToFile());

        Metainfo mFile = null;
        try {
            mFile = new Metainfo("test-file.jtorrent");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println(mFile.JSON);
    }
}
