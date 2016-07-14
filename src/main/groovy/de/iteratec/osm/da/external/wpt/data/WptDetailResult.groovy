package de.iteratec.osm.da.external.wpt.data

import de.iteratec.osm.da.external.FetchJob

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
    long epochTimeCompleted

    List<Step> steps

    public WptDetailResult(FetchJob fetchJob){
        this.jobGroupID = fetchJob.jobGroupId
        this.wptTestID = fetchJob.currentId
        this.wptBaseUrl = fetchJob.wptBaseURL
        this.osmInstance = fetchJob.osmInstance
    }

    /**
     * Analyzes the steps and mark the calculated median step with step.isMedian=true
     */
    void markMedianRuns(){
        //TODO implement median algorithm
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
                ", epochTimeCompleted=" + epochTimeCompleted +
                ", steps=" + steps +
                '}';
    }
}
