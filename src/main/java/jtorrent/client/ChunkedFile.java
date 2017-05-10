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
 * Wrapper class that contains a file being shared by peers, allowing them to read and write
 * the file in chunks. Written in a thread safe manner.
 *
 * @author Xavier McNulty
 * Created by Xavier on 5/1/17.
 */
public class ChunkedFile {
  
    //

    /**
     * Given as a solution on: http://stackoverflow.com/questions/4777622/creating-a-list-of-pairs-in-java
     * Not concurrent in itself.
     * @param <L> Left item in Pair.
     * @param <R> Right item in Pair.
     */
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

    /**
     * Creates a chunked file to be used by a {@link Client client}.
     * @param info Information about the file.
     * @param file The file itself.
     * @param seeding True if the file is being seeded.
     */
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
     * Reads a particular chunk of the file.
     * @param chunkHash SHA-1 hash of the chunk to be read.
     * @return Read chunk of the file.
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
     * @param chunkHash SHA-1 Hash of the chunk to write to.
     * @param data Data to write.
     */
    public synchronized void writeChunk(String chunkHash, InputStream data) throws IOException {
        if (data == null)
            return;

        Pair<Long, Long> byteRange = chunks.get(chunkHash);

        if (byteRange == null)
            return;

        FileOutputStream fos = new FileOutputStream(file);

        fos.getChannel().position(byteRange.getL());
        byte[] buf = new byte[(int) chunkSize];
        int totalWritten = 0;
        int read = 0;

        while (totalWritten < chunkSize && (read = data.read(buf)) > 0) {
            totalWritten += read;

            fos.write(buf, totalWritten, read);
        }

        fos.close();

        writtenChunks.add(chunkHash);

        written += totalWritten;
        left -= totalWritten;

        if (left == 0)
            seeding = true; // we can now seed
    }

    /**
     * Returns a list of chunks that are still needed of the file.
     * @return Set of SHA-1 chunk hashes that are still need for the file to be complete.
     */
    public synchronized Set<String> neededChunks() {
        Set<String> needed = chunks.keySet();
        needed.removeAll(writtenChunks);

        return needed;
    }

    /**
     * Byte's written to the file.
     * @return Current count of bytes written to file.
     */
    public synchronized long getWritten() {
        return written;
    }

    /**
     * Number of bytes that are needed to complete the file.
     * @return Byte count of remaining bytes needed for the file.
     */
    public synchronized long getLeft() {
        return left;
    }

    /**
     * Seeding state of this file.
     * @return {@code true} if seeding.
     */
    public synchronized boolean isSeeding() {
        return seeding;
    }
}
