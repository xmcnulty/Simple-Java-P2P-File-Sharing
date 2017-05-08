package jtorrent.protocols.bittorrent.thp;

import jtorrent.common.JPeer;
import jtorrent.tracker.JTracker;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

/**
 * RMI implementation for Tracker server to handle announce requests from clients.
 * Rather than receiving a request of a socket, a stub to an announce handler that has
 * a reference to the tracker server will be given to clients. This allows clients to
 * make remote calls when they need to announce.
 *
 * Created by Xavier on 5/8/17.
 */
public class AnnounceHandlerRMI implements THPRemote {

    private final ConcurrentMap<String, JTracker.TorrentRef> TORRENTS;
    private final JTracker TRACKER;

    /**
     * Constructor.
     * @param TORRENTS Map of torrents that the owning tracker is watching.
     * @param TRACKER The owning Tracker server.
     */
    public AnnounceHandlerRMI(ConcurrentMap<String, JTracker.TorrentRef> TORRENTS, JTracker TRACKER) {
        this.TORRENTS = TORRENTS;
        this.TRACKER = TRACKER;
    }

    /**
     * Announces to the tracker server.
     * @param peer JPeer making the request
     * @param info_hash Info hash of the torrent that is being announce on.
     * @param event Event of this announce message.
     * @return Collection of available seeding peers.
     * @throws RemoteException
     * @throws IllegalArgumentException
     */
    @Override
    public Collection<JPeer> announce(JPeer peer, String info_hash, Event event) throws RemoteException,
            IllegalArgumentException {
        if (peer == null || peer.getPeerId() == null)
            throw new IllegalArgumentException("Peer id is null");

        // find the torrent.
        JTracker.TorrentRef torrent;

        if (info_hash == null || (torrent = TORRENTS.get(info_hash)) == null)
            throw new IllegalArgumentException("Invalid info hash");

        // Parse the event.
        JTracker.PeerRef peerRef = torrent.getPeer(peer.getPeerId());
        if ((event == null || event == Event.NONE) && peerRef == null)
            event = Event.STARTED;

        if (event != null && event != Event.STARTED && peerRef == null)
            throw new IllegalStateException("Bad state");


        // perform appropriate action based on event
        switch (event) {
            case STARTED:
                peerRef = torrent.peerStarted(peer.getIp(), peer.getPort(), peer.getPeerId(),
                        peer.getBytesUploaded(), peer.getBytesDowloaded(), peer.getBytesLeft());
                break;
            case COMPLETED:
                peerRef = torrent.peerCompleted(peer.getPeerId(), peer.getBytesUploaded(),
                        peer.getBytesDowloaded(), peer.getBytesLeft());
                break;
            case STOPPED:
                peerRef = torrent.peerStopped(peer.getPeerId(), peer.getBytesUploaded(),
                        peer.getBytesDowloaded(), peer.getBytesLeft());
                break;
            default:
                peerRef = torrent.peerDefaultAnnounce(peer.getPeerId(), peer.getBytesUploaded(),
                        peer.getBytesDowloaded(), peer.getBytesLeft());
        }

        // return valid peers
        return torrent.getValidPeers(peerRef);
    }
}
