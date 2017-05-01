package jtorrent.client;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Serves file chunks to those who request them.
 * Created by Xavier on 5/1/17.
 */
public class SeederHandler implements org.simpleframework.http.core.Container {
    private ChunkedFile file;
    private final Executor executor;

    public SeederHandler(ChunkedFile file) {
        this.file = file;
        executor = Executors.newFixedThreadPool(5);
    }

    @Override
    public void handle(Request request, Response response) {
        System.out.println("Received seed request.");
        executor.execute(new Reader(request, response));
    }

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
