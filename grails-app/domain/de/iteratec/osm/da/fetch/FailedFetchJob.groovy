package de.iteratec.osm.da.fetch

class FailedFetchJob {

    FetchFailReason reason
    String message
    @Delegate FetchJob originalJob
    static constraints = {
        reason nullable: false
        message nullable: false
        fetchBatch nullable: true
    }
}
