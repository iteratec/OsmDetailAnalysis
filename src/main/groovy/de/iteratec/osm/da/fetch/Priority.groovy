package de.iteratec.osm.da.fetch

/**
 * Used to describe a priority order among FetchJob
 */
enum Priority {
    //The values have some space between, so we can finer granulated priorities, if needed
    Low(30),
    Normal(60),
    High(90)

    int value

    private Priority(int value) {
        this.value = value
    }

    @Override
    String toString() {
        return value
    }

}