package de.iteratec.osm.da.report.external

/**
 * @author nkuhn
 */
class GraphitePathName {
    /**
     * <p>
     * Replaces characters, not allowed in Graphite-paths with allowed ones.
     * <ul>
     * <li>spaces get replaced through underscore</li>
     * <li>no word characters ( so ![a-zA-Z_0-9] ) get replaced through minus
     * sign</li>
     * </ul>
     * </p>
     *
     * @param pathName
     *            A raw path, non escaped path name, not <code>null</code>.
     * @return A escaped path name, never <code>null</code>.
     */
    private static String escapeGraphitePathName(String pathName) {
        pathName = pathName.replaceAll("\\s", "_"); // space -> "_"
        pathName = pathName.replaceAll("[^\\w\\.]", "-"); // no word character (
        // so ![a-zA-Z_0-9])
        // -> "-"
        return pathName;
    }

    /**
     * <p>
     * Delivers the Graphite path name for the given path elements
     * {@link String}-values.
     * </p>
     *
     * @param pathElements
     *            The {@linkplain #toString() string value} of a path elements,
     *            not <code>null</code>, not {@linkplain String#isEmpty() empty}
     *            , may not contain a '.' (dot).
     * @return An approximate path name, never <code>null</code>.
     * @throws IllegalArgumentException
     *             if at least one of the {@code pathElements} is
     *             {@linkplain String#isEmpty() empty} or contains at least one
     *             dot.
     */
    public static GraphitePathName valueOf(final String... pathElements) {

        StringBuilder resultBuilder = new StringBuilder();
        boolean afterFirstElement = false;

        for (String eachPathElement : pathElements) {

            if (eachPathElement.isEmpty()) {
                throw new IllegalArgumentException(
                        "A graphite path element could not be empty.");
            }

            if (eachPathElement.contains(".")) {
                throw new IllegalArgumentException(
                        "A graphite path element could not contain a dot.");
            }

            if (afterFirstElement) {
                resultBuilder.append(".");
            }
            resultBuilder.append(eachPathElement);
            afterFirstElement = true;
        }

        return valueOf((String)resultBuilder.toString());
    }

    /**
     * <p>
     * Delivers the Graphite path name for the given {@link String}-value.
     * </p>
     *
     * <p>
     * FIXME mze 2013-11-06. Das Graphite path name format nochmal nachforschen.
     * Wohlmöglich dürfen die Elemente (zwischen den Dots) nur aus a-zA-Z0-9
     * bestehen, aber das geht nicht klar aus der Doku hervor!? Also nur etwas
     * in der Art {@code wpt.lhotse.avgh1.hp.csi} anstatt etwas wie
     * {@code wpt.lhotse[ ]group.avgh1.hp.csi.} (mit Leerzeichen).
     * </P>
     *
     * @param stringValueOfPathName
     *            The {@linkplain #toString() string value} of a path name, not
     *            <code>null</code>, not {@linkplain String#isEmpty() empty}.
     * @return An approximate path name, never <code>null</code>.
     * @throws IllegalArgumentException
     *             if {@code stringValueOfPathName} is
     *             {@linkplain String#isEmpty() empty} or contains at least one
     *             sequence of dots without other characters between it (double
     *             dots, 3 dots, ...).
     */
    public static GraphitePathName valueOf(final String stringValueOfPathName) {

        if (stringValueOfPathName.isEmpty()) {
            throw new IllegalArgumentException(
                    "A graphite pathname could not be empty.");
        }

        // Check for double dots:
        if (stringValueOfPathName.contains("..")) {
            throw new IllegalArgumentException(
                    "A graphite pathname could not contain double dots.");
        }

        return new GraphitePathName(
                escapeGraphitePathName(stringValueOfPathName));
    }

    private final String stringValueOfPathName;

    private GraphitePathName(final String stringValueOfPathName) {
        this.stringValueOfPathName = stringValueOfPathName;
    }

    /**
     * <p>
     * Checks weather this path name is equal to another one.
     * </p>
     *
     * <p>
     * Two path names are considered as equal if and only if their
     * {@link #toString()} result is equal.
     * </p>
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GraphitePathName)) {
            return false;
        }
        final GraphitePathName other = (GraphitePathName) obj;
        if (this.stringValueOfPathName == null) {
            if (other.stringValueOfPathName != null) {
                return false;
            }
        } else if (!this.stringValueOfPathName
                .equals(other.stringValueOfPathName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
        + ((this.stringValueOfPathName == null) ? 0
                : this.stringValueOfPathName.hashCode());
        return result;
    }

    /**
     * <p>
     * The {@link String} value of this path name which is equivalent to the
     * path in Graphite.
     * </p>
     */
    @Override
    public String toString() {
        return this.stringValueOfPathName;
    }
}
