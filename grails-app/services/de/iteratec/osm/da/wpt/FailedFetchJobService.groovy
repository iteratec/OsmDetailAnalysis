package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.fetch.FailedFetchJob
import de.iteratec.osm.da.fetch.FetchFailReason
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.resolve.exceptions.WptResultProcessingException

class FailedFetchJobService {


    public FailedFetchJob markJobAsFailed(FetchJob job, Exception e){
        FailedFetchJob failedFetchJob
        if(e instanceof WptResultProcessingException){
            failedFetchJob = createFailedFetchJob(job, e.fetchFailReason ,e.message)
        } else{
            failedFetchJob = createFailedFetchJob(job, FetchFailReason.UNKOWN, e.message)
        }
        log.error("Job with id ${job?.id} encountert an error while trying to get the following result:\n" +
                    "'${job.getWptBaseURL()}/jsonResult.php?test=${job.wptTestId}&requests=1&multiStepFormat=1'."+
                    "Reason: ${failedFetchJob.reason}, Message: ${failedFetchJob.message}")
        job.delete(failOnError: true)
        return failedFetchJob
    }


    public FailedFetchJob createFailedFetchJob(FetchJob fetchJob, FetchFailReason reason, String message){
        FailedFetchJob failedFetchJob = new FailedFetchJob(reason: reason, originalJob: fetchJob, message: message)
        return failedFetchJob.save(flush:true, failOnError:true)
    }

}
