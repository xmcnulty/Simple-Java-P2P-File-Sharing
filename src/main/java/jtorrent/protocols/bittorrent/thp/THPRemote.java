package jtorrent.protocols.bittorrent.thp;

import jtorrent.common.JPeer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Interface defining the available remote methods for a RMI announce handler.
 * Created by Xavier on 5/8/17.
 */
public interface THPRemote extends Remote {
    Collection<JPeer> announce(JPeer peer, String info_hash, Event event) throws RemoteException,
            IllegalArgumentException;
}
