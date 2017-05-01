package jtorrent.client;

import javafx.util.Pair;
import jtorrent.common.Utils;
import jtorrent.protocols.bittorrent.metainfo.InfoDictionary;

import java.io.*;
import java.util.Dictionary;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Chunks a file.
 * Created by Xavier on 5/1/17.
 */
public class FileChunk {
    private final String name;
    private final long size;
    private final int chunkSize;
    private final int numChunks;

    private final File file;
    private final ConcurrentMap<String, Pair<Long, Long>> chunks; // Piece hash to piece byte range

    private boolean seeding;

    public FileChunk(InfoDictionary info, File file, boolean seeding) {
        this.file = file;

        chunks = new ConcurrentHashMap<>();

        Dictionary<String, Object> infoValues = info.get();
        name = (String) infoValues.get(InfoDictionary.NAME_KEY);
        size = (Long) infoValues.get(InfoDictionary.LENGTH_KEY);
        chunkSize = (Integer) infoValues.get(InfoDictionary.PIECE_LENGTH_KEY);
        byte[][] pieceHashes = (byte[][]) infoValues.get(InfoDictionary.PIECES_KEY);

        numChunks = pieceHashes.length;

        for (long i=0, c=0; i < numChunks; i++, c += chunkSize) {
            chunks.put(Utils.bytesToHex(pieceHashes[(int) i]), new Pair<Long, Long>(c, c + chunkSize - 1));
        }

        this.seeding = seeding;
    }

    /**
     * Reads a particular chunk of the file
     * @param chunkHash Hash of the chunk
     * @return the chunk
     * @throws IOException
     */
    public synchronized byte[] readChunk(String chunkHash) throws IOException {
        if (!seeding) // can't read when we still need to write
            return null;

        FileInputStream fis = new FileInputStream(file);

        Pair<Long, Long> byteRange = chunks.get(chunkHash);

        if (byteRange == null)
            return null;

        fis.getChannel().position(byteRange.getKey());

        byte[] bytes = new byte[chunkSize];

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

        fos.getChannel().position(byteRange.getKey());
        fos.write(data);

        fos.close();
    }

    public synchronized boolean isSeeding() {
        return seeding;
    }

    public synchronized void startSeeding() {
        seeding = true;
    }
}
