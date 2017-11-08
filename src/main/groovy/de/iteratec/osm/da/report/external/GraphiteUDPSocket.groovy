package de.iteratec.osm.da.report.external

import com.codahale.metrics.graphite.GraphiteUDP
/**
 * @author nkuhn
 */
class GraphiteUDPSocket implements GraphiteSocket{
    private final InetAddress serverAddress
    private final int port

    /**
     * <p>
     * Creates a TCP-Socket based Graphite socket.
     * </p>
     *
     * @param serverAddress The server adress to connect to, not <code>null</code>.
     * @param port The port to use for communication; must satisfy
     * {@code 0 <= port <= 65535}.
     * @throws NullPointerException if {@code serverAddress} is <code>null</code>.
     * @throws {@link IllegalArgumentException} if {@code port} is less than 0
     *                              or greater than 65353.
     */
    GraphiteUDPSocket(InetAddress serverAddress, int port) throws IllegalArgumentException {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("The port number must be between 0 and 65535 (both inclusive).")
        }else if(!serverAddress){
            throw new IllegalArgumentException("Argument serverAdress may not be null!")
        }

        this.serverAddress = serverAddress
        this.port = port
    }

    @Override
    void sendDate(GraphitePathName path, double value, Date timestamp)
            throws NullPointerException, GraphiteComunicationFailureException {

        GraphiteUDP graphiteSocket
        try {
            graphiteSocket = new GraphiteUDP(serverAddress.getHostAddress(), this.port)
            graphiteSocket.connect()
            // use seconds
            long metricTimestamp = timestamp.getTime() / 1000
            graphiteSocket.send(path.toString(), String.valueOf(value), metricTimestamp)
        } catch (IOException cause) {
            throw new GraphiteComunicationFailureException(serverAddress, port, cause)
        } finally {
            if (graphiteSocket) {
                graphiteSocket.close()
            }
        }
    }
}
