package de.iteratec.osm.da.fetch

class FailedFetchJob {

    FetchFailReason reason
    @Delegate FetchJob originalJob
    static constraints = {
        reason nullable: false
        fetchBatch nullable: true
    }
}
