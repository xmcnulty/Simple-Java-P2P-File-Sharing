package jtorrent.client;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Listens to incoming socket connections from other peers requesting file chunks.
 * Uses an {@link Executor exectutor} to process requests in parallel.
 *
 * @author Xavier McNulty
 * Created by Xavier on 5/1/17.
 */
public class SeederHandler implements org.simpleframework.http.core.Container {
    private ChunkedFile file;
    private final Executor executor;

    /**
     * Constructor.
     * @param file File.
     */
    public SeederHandler(ChunkedFile file) {
        this.file = file;
        executor = Executors.newFixedThreadPool(5);
    }

    /**
     * Called for each incoming request.
     * @param request
     * @param response
     */
    @Override
    public void handle(Request request, Response response) {
        System.out.println("Received seed request.");
        executor.execute(new Reader(request, response));
    }

    /**
     * Private Runnable class that allows the handler to serve requests in parallel.
     */
    private class Reader implements Runnable {
        Request request;
        Response response;

        public Reader(Request request, Response response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public void run() {
            String chunkHash = request.getPath().toString().replace("/", "");

            response.setCode(200);
            response.setContentType("application/octet-stream");
            response.setDescription(chunkHash);
            try {
                OutputStream os = response.getOutputStream();
                os.write(file.readChunk(chunkHash));
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
