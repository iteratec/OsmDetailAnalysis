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
    List<FetchFailure> failureList = []
    Date creationDate = new Date()
    public final static MIN_SECONDS_BETWEEN_CALLBACKS = 30

    def addFailure(FetchJob fetchJob){
        failureList.add(new FetchFailure( wptBaseURL: fetchJob.wptBaseURL,
                wptTestId:fetchJob.wptTestId,
                wptVersion:fetchJob.wptVersion,
                fetchBatch: this).save(flush:true))
    }
    static constraints = {

    }
}
