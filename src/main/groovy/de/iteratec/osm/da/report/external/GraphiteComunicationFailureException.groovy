package de.iteratec.osm.da.report.external

/**
 * @author nkuhn
 */
class GraphiteComunicationFailureException {
    /**
     * Required serial version UID.
     */
    private static final long serialVersionUID = 2364690032165010639L;

    private final InetAddress serverAddress;
    private final int port;

    /**
     * <p>
     * Creates a new exception for a communication failure with the specified
     * server, the specified port and takes the reason. None of the arguments
     * can be empty.
     * </p>
     *
     * @param serverAddress
     * @param port
     * @param cause
     *
     * @throws NullPointerException
     *             if {@code serverAddress} is <code>null</code>.
     */
    public GraphiteComunicationFailureException(InetAddress serverAddress,
                                                int port, IOException cause) throws NullPointerException {
        super("Failed to communicate with server " + serverAddress.toString()
                + " on port " + port, cause);
        this.serverAddress = serverAddress;
        this.port = port;
    }

    /**
     * <p>
     * The {@link InetAddress} of the server tried to communicate with.
     * </p>
     *
     * @return not <code>null</code>.
     */
    public InetAddress getServerAddress() {
        return serverAddress;
    }

    /**
     * <p>
     * The port used to try a communication.
     * </p>
     *
     * @return not <code>null</code>.
     */
    public int getPort() {
        return port;
    }
}
