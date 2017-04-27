package jtorrent.protocols.bittorrent.thp;

import jtorrent.common.JTorrent;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import java.util.concurrent.ConcurrentMap;

/**
 * Handles Announce messages for a container using the simpleframework.http
 * library for handling http messages on the URL http://ip:port/announce
 * of the tracker.
 *
 * Keeps a reference to a map of torrents known to the tracker.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/27/17.
 */
public class AnnounceHandler implements org.simpleframework.http.core.Container {

    public static final String ANNOUNCE_PATH = "/announce"; // HTTP url path for announce requests

    private final ConcurrentMap<String, JTorrent> TORRENTS;

    public AnnounceHandler(ConcurrentMap<String, JTorrent> TORRENTS) {
        this.TORRENTS = TORRENTS;
    }

    @Override
    public void handle(Request request, Response response) {
        if (!ANNOUNCE_PATH.equals(request.getPath().toString())) {
            response.setCode(404);
            response.setDescription("Not Found");
            return;
        }

        // TODO: Need to parse the request which will be sent over JSON.
    }
}
