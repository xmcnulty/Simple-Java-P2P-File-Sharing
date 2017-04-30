package jtorrent.protocols.bittorrent.thp;

import com.google.gson.Gson;
import jtorrent.common.JPeer;
import jtorrent.protocols.bittorrent.metainfo.Metainfo;
import jtorrent.tracker.JTracker;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
    public static final String NEW_TORRENT_PATH = "/new_torrent";

    private final ConcurrentMap<String, JTracker.TorrentRef> TORRENTS;
    private final JTracker TRACKER;

    public AnnounceHandler(ConcurrentMap<String, JTracker.TorrentRef> TORRENTS, JTracker tracker) {
        this.TORRENTS = TORRENTS;
        this.TRACKER = tracker;
    }

    @Override
    public void handle(Request request, Response response) {
        if (NEW_TORRENT_PATH.equalsIgnoreCase(request.getPath().toString())) {
            try {
                InputStream inputStream = request.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                Metainfo metainfo = (Metainfo) objectInputStream.readObject();

                TRACKER.addTorrent(metainfo);
            } catch (Exception e) {
                response.setCode(400);
                response.setDescription("Error grabbing object.");
                return;
            }

            response.setCode(200);
            return;
        }

        if (!ANNOUNCE_PATH.equals(request.getPath().toString())) {
            response.setCode(404);
            response.setDescription("Not Found");
            return;
        }

        try {
            processAnnounce(request.getContent(), response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes the announce request and builds a response.
     * @param body JSON body of the original announce.
     * @param response Response to the announce.
     */
    private void processAnnounce(String body, Response response) throws IOException {

        Map<String, Object> rootMapJson = new Gson().fromJson(body, Map.class);

        // get the peer id and info hash.
        String peer_id = (String) rootMapJson.get("peer_id");
        String info_hash = (String) rootMapJson.get("info_hash");

        if (peer_id == null) {
            response.setContentType("text/plain");
            response.setCode(400);
            response.setDescription("No peer_id.");

            return;
        }

        // The above fields are required, need to send back an error if they don't exist.
        JTracker.TorrentRef torrent;
        if (info_hash == null || (torrent = TORRENTS.get(info_hash)) == null) {
            response.setContentType("text/plain");
            response.setCode(400);
            response.setDescription("Invalid info_hash.");

            return;
        }

        // get the event.
        String eventStr = (String) rootMapJson.get("event");
        Event event = null;

        // this is the first time this peer is announcing for this torrent.
        if ((eventStr == null || (event = Event.fromString(eventStr)) == Event.NONE)
                && torrent.getPeer(peer_id) == null) {
            event = Event.STARTED; // the default peer state if no valid event sent in announce.
        }

        // something went wrong.
        if (event != null && event != Event.STARTED && torrent.getPeer(peer_id) == null) {
            response.setContentType("text/plain");
            response.setCode(400);
            response.setDescription("Bad request.");

            return;
        }

        // everything seems to be in order, lets update the peer
        JTracker.PeerRef peer = null;

        switch (event) {
            case STARTED:
                peer = torrent.peerStarted((String) rootMapJson.get("ip"),
                        (Integer) rootMapJson.get("port"),
                        peer_id, (Long) rootMapJson.get("updated"),
                        (Long) rootMapJson.get("downloaded"),
                        (Long) rootMapJson.get("left"));
                break;
            case STOPPED:
                peer = torrent.peerStopped(peer_id, (Long) rootMapJson.get("updated"),
                        (Long) rootMapJson.get("downloaded"),
                        (Long) rootMapJson.get("left"));
                break;
            case COMPLETED:
                peer = torrent.peerCompleted(peer_id, (Long) rootMapJson.get("updated"),
                        (Long) rootMapJson.get("downloaded"),
                        (Long) rootMapJson.get("left"));
                break;
            default:
                peer = torrent.peerDefaultAnnounce(peer_id, (Long) rootMapJson.get("updated"),
                        (Long) rootMapJson.get("downloaded"),
                        (Long) rootMapJson.get("left"));
                break;
        }

        response.setContentType("application/json");
        response.setCode(200);

        // Write the response JSON.
        PrintStream responseStream = response.getPrintStream();
        responseStream.print(craftResponseBody(torrent, torrent.getValidPeers(peer)));
        responseStream.flush();
        responseStream.close();
    }

    /**
     * Crafts the JSON response body.
     * @param torrent
     * @return
     */
    private String craftResponseBody(JTracker.TorrentRef torrent, Collection<JPeer> responsePeers) {
        Map<String, Object> responseMap = new HashMap<>();

        ArrayList<Map<String, Object>> responsePeerData = new ArrayList<>(); // data of peers sent in response.

        responsePeers.forEach(peer -> {
            responsePeerData.add(peer.getResponseFields());
        });

        responseMap.put("interval", JTracker.TorrentRef.ANNOUNCE_INTERVAL_SECONDS);
        responseMap.put("peers", responsePeerData);

        return new Gson().toJson(responseMap);
    }
}
