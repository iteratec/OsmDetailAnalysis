package de.iteratec.osm.da.fetch

class FetchBatch {
    Date lastUpdate = new Date()
    int lastValue = 0
    boolean queuingDone = false
    String callbackUrl
    String osmUrl
    int callBackId
    int countFetchJobs = 0
    List<FetchJob> fetchJobs = []
    int failures = 0
    Date creationDate = new Date()
    public final static MIN_SECONDS_BETWEEN_CALLBACKS = 30

    def addFailure(){
        failures++
    }
    static constraints = {

    }
}
