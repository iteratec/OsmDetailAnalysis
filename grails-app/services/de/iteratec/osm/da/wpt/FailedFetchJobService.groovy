package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.fetch.FailedFetchJob
import de.iteratec.osm.da.fetch.FetchFailReason
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.data.Request
import de.iteratec.osm.da.wpt.data.Step
import de.iteratec.osm.da.wpt.data.WptDetailResult
import grails.transaction.Transactional

@Transactional
class FailedFetchJobService {

    public FailedFetchJob markJobAsFailedIfNeeded(WptDetailResult result, FetchJob job, FetchFailReason reason = null){
        if(!reason){
            if(!result.hasSteps()){
               reason = FetchFailReason.NO_STEPS_FOUND
            } else if(hasMissingValues(result)){
                reason = FetchFailReason.MISSING_VALUES
            }else{
                return null
            }
        }
        FailedFetchJob failedFetchJob = createFailedFetchJob(job,reason)
        return failedFetchJob
    }

    public FailedFetchJob createFailedFetchJob(FetchJob fetchJob, FetchFailReason reason){
        FailedFetchJob failedFetchJob = new FailedFetchJob(reason: reason, originalJob: fetchJob)
        return failedFetchJob.save(flush:true, failOnError:true)
    }

    public boolean hasMissingValues(WptDetailResult result){
        boolean missing = false
        result.steps.each {Step step ->
            missing &= step.hasRequests()
            missing &= step.hasMetaValues()
            if(missing){
                println "aha!"
            }
            step.requests.each {Request request->
                missing &= request.hasValues()
            }
        }
        return missing
    }


}
