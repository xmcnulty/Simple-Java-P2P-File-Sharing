package jtorrent.client;

//import javafx.util.Pair;
import jtorrent.common.Utils;
import jtorrent.protocols.bittorrent.metainfo.InfoDictionary;

import java.io.*;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Chunks a file.
 * Created by Xavier on 5/1/17.
 */
public class ChunkedFile {
  
    // Given as a solution on: http://stackoverflow.com/questions/4777622/creating-a-list-of-pairs-in-java
    // Not concurrent in itself
    public class Pair<L,R> {
      private L l;
      private R r;
      public Pair(L l, R r){
          this.l = l;
          this.r = r;
      }
      public L getL(){ return l; }
      public R getR(){ return r; }
      public void setL(L l){ this.l = l; }
      public void setR(R r){ this.r = r; }
    }
  
    private final String name;
    private final long size;
    private final long chunkSize;
    private final int numChunks;

    private final File file;
    private final ConcurrentMap<String, Pair<Long, Long>> chunks; // Piece hash to piece byte range

    private long written, left; // number of byte's written and left to write.
    private Set<String> writtenChunks;

    private boolean seeding;

    public ChunkedFile(InfoDictionary info, File file, boolean seeding) {
        this.file = file;

        chunks = new ConcurrentHashMap<String, Pair<Long, Long>>();

        Dictionary<String, Object> infoValues = info.get();
        name = (String) infoValues.get(InfoDictionary.NAME_KEY);
        size = (Long) infoValues.get(InfoDictionary.LENGTH_KEY);
        chunkSize = (Long) infoValues.get(InfoDictionary.PIECE_LENGTH_KEY);
        byte[][] pieceHashes = (byte[][]) infoValues.get(InfoDictionary.PIECES_KEY);

        numChunks = pieceHashes.length;

        for (long i=0, c=0; i < numChunks; i++, c += chunkSize) {
            chunks.put(Utils.bytesToHex(pieceHashes[(int) i]), new Pair<Long, Long>(c, c + chunkSize - 1));
        }

        this.seeding = seeding;
        writtenChunks = new HashSet<>();

        if (this.seeding) {
            writtenChunks.addAll(chunks.keySet());
            written = size;
            left = 0;

            System.out.println(chunks);
        } else {
            written = 0;
            left = size;
        }
    }

    /**
     * Reads a particular chunk of the file
     * @param chunkHash Hash of the chunk
     * @return the chunk
     * @throws IOException
     */
    public byte[] readChunk(String chunkHash) throws IOException {
        synchronized (this) {
            if (!seeding) // can't read when we still need to write
                return null;
        }

        // no need to acquire lock, we can read concurrently, because one a file is
        // completely written it will not be modified.
        FileInputStream fis = new FileInputStream(file);

        Pair<Long, Long> byteRange = chunks.get(chunkHash);

        if (byteRange == null)
            return null;

        fis.getChannel().position(byteRange.getL());

        byte[] bytes = new byte[(int) chunkSize];

        int numRead = fis.read(bytes);
        fis.close();

        if (numRead < chunkSize) {
            byte[] bytes1 = new byte[numRead];

            for (int i=0; i < numRead; i++)
                bytes1[i] = bytes[i];

            return bytes1;
        } else {
            return bytes;
        }
    }

    /**
     * Writes data to a chunk of the file.
     * @param chunkHash Hash of the chunk to write to.
     * @param data Data to write.
     */
    public synchronized void writeChunk(String chunkHash, byte[] data) throws IOException {
        if (data == null || data.length > chunkSize)
            return;

        Pair<Long, Long> byteRange = chunks.get(chunkHash);

        if (byteRange == null)
            return;

        FileOutputStream fos = new FileOutputStream(file);

        fos.getChannel().position(byteRange.getL());
        fos.write(data);

        fos.close();

        writtenChunks.add(chunkHash);

        written += data.length;
        left -= data.length;

        if (left == 0)
            seeding = true; // we can now seed
    }

    /**
     * Returns a list of chunks that are still needed of the file.
     * @return
     */
    public synchronized Set<String> neededChunks() {
        Set<String> needed = chunks.keySet();
        needed.removeAll(writtenChunks);

        return needed;
    }

    public synchronized long getWritten() {
        return written;
    }

    public synchronized long getLeft() {
        return left;
    }

    public synchronized boolean isSeeding() {
        return seeding;
    }
}
