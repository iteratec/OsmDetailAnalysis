package de.iteratec.osm.da.report.external

/**
 * @author nkuhn
 */
interface GraphiteSocket {
    /**
     * <p>
     * Sends the specified data to Graphite.
     * </p>
     *
     * @param path
     *            The path where the data-value is to be stored in, not
     *            <code>null</code>.
     * @param value
     *            The data-value to send.
     * @param timestamp
     *            The time-stamp the data-value belongs to. Note: Only the
     *            seconds (UNiX System V time-stamp) of {@link Date#getTime()}
     *            are recognized.
     *
     * @throws NullPointerException
     *             if at least one arguement is <code>null</code>.
     * @throws GraphiteComunicationFailureException
     *             if a communication failure with the graphite server occured.
     *
     */
    void sendDate(GraphitePathName path, double value, Date timestamp)
            throws NullPointerException, GraphiteComunicationFailureException;
}