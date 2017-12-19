package de.iteratec.osm.da.report.external

import java.nio.charset.Charset

/**
 * @author nkuhn
 */
class GraphiteTCPSocket implements GraphiteSocket {
    private static final byte CAN = 0x24;
    private static final byte LF = 0x0a;

    private final InetAddress serverAddress;
    private final int port;

    /**
     * <p>
     * Creates a TCP-Socket based Graphite socket.
     * </p>
     *
     * @param serverAddress
     *            The server adress to connect to, not <code>null</code>.
     * @param port
     *            The port to use for communication; must satisfy
     * {@code 0 <= port <= 65535}.
     *
     * @throws NullPointerException
     *             if {@code serverAddress} is <code>null</code>.
     * @throws {@link IllegalArgumentException} if {@code port} is less than 0
     *         or greater than 65353.
     */
    public GraphiteTCPSocket(InetAddress serverAddress, int port) throws IllegalArgumentException {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(
                    "The port number must be between 0 and 65535"
                            + " (both inclusive).");
        } else if (!serverAddress) {
            throw new IllegalArgumentException("Argument serverAdress may not be null!")
        }

        this.serverAddress = serverAddress;
        this.port = port;
    }

    @Override
    public void sendDate(GraphitePathName path, double value, Date timestamp)
            throws NullPointerException, GraphiteComunicationFailureException {

        Socket graphiteSocket = null;
        try {
            graphiteSocket = new Socket(serverAddress, this.port);
            //if the server isn't reachable the try to report should stop after 10 seconds
            graphiteSocket.setSoTimeout(3000);

            OutputStream graphiteFeedStream = graphiteSocket.getOutputStream();

            // use seconds, UNiX system V
            long metricTimestamp = timestamp.getTime() / 1000;

            StringBuilder sbGraphiteMessage = new StringBuilder()
                    .append(path.toString())
                    .append(" ")
                    .append(String.valueOf(value))
                    .append(" ")
                    .append(String.valueOf(metricTimestamp))

            byte[] messageToSendToGraphiteInUSASCII = sbGraphiteMessage.toString()
                    .getBytes(Charset.forName("US-ASCII"));

            graphiteFeedStream.write(messageToSendToGraphiteInUSASCII);
            graphiteFeedStream.write(LF);
            graphiteFeedStream.write(CAN);

            graphiteFeedStream.flush();
            graphiteSocket.close();
        } catch (IOException cause) {
            throw new GraphiteComunicationFailureException(serverAddress, port,
                    cause);
        } finally {
            if (graphiteSocket != null) {
                try {
                    graphiteSocket.close();
                } catch (IOException ignored) {
                    // Ignored, we've just tried it.
                }
            }
        }
    }
}
