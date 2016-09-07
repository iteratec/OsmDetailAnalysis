package de.iteratec.osm.da.fetch

class FetchFailure {
    FetchBatch fetchBatch
    String wptBaseURL
    String wptTestId
    String wptVersion
    Date timeOfFailure = new Date()

    static constraints = {
    }
}
