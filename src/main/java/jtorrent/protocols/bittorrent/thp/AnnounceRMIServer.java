package jtorrent.protocols.bittorrent.thp;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Server to host RMI announce handler of a tracker.
 * Created by Xavier on 5/8/17.
 */
public class AnnounceRMIServer {
    private Registry registry;
    private final AnnounceHandlerRMI announceHandler;
    private static final String NAME = "announce-handler";

    /**
     * Construct a server with an announce handler.
     * @param announceHandler AnnounceHandlerRMI
     */
    public AnnounceRMIServer(AnnounceHandlerRMI announceHandler) {
        this.announceHandler = announceHandler;
    }

    /**
     * Attempts to start hosting this handler.
     * @param port Port number for registry.
     */
    public synchronized void start(int port) throws RemoteException {
        if (registry != null)
            throw new IllegalStateException("Server already running.");

        Registry reg;
        reg = LocateRegistry.createRegistry(port);

        reg.rebind(NAME, announceHandler);
        registry = reg;

        System.out.println("RMI announce handler running on port: " + port);
    }

    /**
     * Stop the server.
     */
    public synchronized void stop() {
        if (registry != null) {
            try {
                registry.unbind(NAME);
            } catch (Exception e) {
                System.err.printf("unable to stop: %s%n", e.getMessage());
            } finally {
                registry = null;
            }
        }
    }

    public synchronized boolean isRunning() {
        return registry != null;
    }
}
