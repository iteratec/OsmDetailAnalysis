package de.iteratec.osm.da.wpt.data

import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.LoadPhase

/**
 * This represents the whole necessary data which to save the detail data.
 */
class WptDetailResult {


    int bandwidthDown
    int bandwidthUp
    int latency
    int packagelossrate
    String location
    String browser
    int jobGroupID
    String wptBaseUrl
    String wptTestID
    long osmInstance
    long jobId

    List<Step> steps

    public WptDetailResult(FetchJob fetchJob){
        this.jobId = fetchJob.jobId
        this.jobGroupID = fetchJob.jobGroupId
        this.wptTestID = fetchJob.wptTestId
        this.wptBaseUrl = fetchJob.wptBaseURL
        this.osmInstance = fetchJob.osmInstance
    }

    /**
     * Calculates the median runs and set the phases for all requests
     */
    void calculateAdditionalInformations(){
        markMedianRuns()
        setPhase()
    }

    /**
     * Analyzes the steps and mark the calculated median step with step.isMedian=true
     */
    void markMedianRuns(){
         steps.groupBy {it.stepNumber}.each {int stepNumber,List<Step> steps ->
             int medianPlace = Math.ceil(steps.size()/2) -1 as Integer
             steps.sort{it.domTime}
             steps[medianPlace].isMedian = true
        }
    }

    /**
     * Analyzes all request and mark the loading start and end phase as doctime, loadtime or fully loaded
     */
    void setPhase(){
        steps.each {Step step ->
            step.requests.each {Request request ->
                request.setPhases(step.domTime,step.loadTime)
            }
        }
    }


    @Override
    public String toString() {
        return "WptDetailResult{" +
                ", bandwidthDown=" + bandwidthDown +
                ", bandwidthUp=" + bandwidthUp +
                ", latency=" + latency +
                ", packagelossrate=" + packagelossrate +
                ", location='" + location + '\'' +
                ", browser='" + browser + '\'' +
                ", jobGroupID=" + jobGroupID +
                ", wptBaseUrl='" + wptBaseUrl + '\'' +
                ", wptTestID='" + wptTestID + '\'' +
                ", osmInstance=" + osmInstance +
                ", steps=" + steps +
                '}';
    }
}
