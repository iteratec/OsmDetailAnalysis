package de.iteratec.osm.da.report.external.provider

import de.iteratec.osm.da.report.external.GraphiteServer
import de.iteratec.osm.da.report.external.GraphiteSocket

/**
 * @author nkuhn
 */
interface GraphiteSocketProvider {
    /**
     * Supported protocols for Graphite sockets.
     *
     * @author mze
     */
    enum Protocol {
        TCP, UDP
    }

    /**
     * Returns a {@link GraphiteSocket} for given server. Protocol
     * of returned socket is UDP or TCP respective {@link GraphiteServer#reportProtocol}.
     *
     * @param server
     *            the server to connect to
     * @return never <code>null</code>.
     */
    GraphiteSocket getSocket(GraphiteServer server);

    /**
     * Returns a {@link Protocol} specific {@link GraphiteSocket}
     *
     * @param server
     *            the server to connect to
     * @param protocol
     *            the reportProtocol to use
     * @return never <code>null</code>.
     */
    GraphiteSocket getSocket(GraphiteServer server, Protocol protocol);

}