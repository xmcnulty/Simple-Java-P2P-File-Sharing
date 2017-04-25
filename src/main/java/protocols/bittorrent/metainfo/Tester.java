package protocols.bittorrent.metainfo;

/**
 * Created by Xavier on 4/24/17.
 */
public class Tester {
    public static void main(String args[]) {
        String[] hashes = {"432344", "3333124"};

        InfoDictionary iDict = new InfoDictionary(100,
                "test-file",
                50,
                hashes);

        Metainfo m = new Metainfo("http://test.com", iDict);

        System.out.println(m.JSON_STR);
    }
}
