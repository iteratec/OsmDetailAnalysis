package de.iteratec.osm.da.report.external.provider

import de.iteratec.osm.da.report.external.GraphiteServer
import de.iteratec.osm.da.report.external.GraphiteSocket
import de.iteratec.osm.da.report.external.GraphiteTCPSocket
import de.iteratec.osm.da.report.external.GraphiteUDPSocket

/**
 * @author nkuhn
 */
class DefaultGraphiteSocketProvider implements GraphiteSocketProvider{

    DefaultGraphiteSocketProvider() {}

    @Override
    GraphiteSocket getSocket(GraphiteServer server) {
        return getSocket(server, server.reportProtocol)
    }

    @Override
    GraphiteSocket getSocket(GraphiteServer server, GraphiteSocketProvider.Protocol protocol) {
        if (protocol == GraphiteSocketProvider.Protocol.TCP) {
            return new GraphiteTCPSocket(server.getServerInetAddress(), server.getPort());
        } else if (protocol == GraphiteSocketProvider.Protocol.UDP) {
            return new GraphiteUDPSocket(server.getServerInetAddress(), server.getPort())
        } else {
            throw new IllegalArgumentException("Unknown Protocol" + protocol);
        }
    }

}
