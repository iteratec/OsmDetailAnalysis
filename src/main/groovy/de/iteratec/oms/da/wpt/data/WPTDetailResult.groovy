package de.iteratec.oms.da.wpt.data

import de.iteratec.osm.da.har.FetchJob

/**
 * This represents the whole necessary data which to save the detail data.
 */
class WPTDetailResult {


    String eventName
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

    List<Step> steps

    public WPTDetailResult(FetchJob fetchJob){
        this.jobGroupID = fetchJob.jobGroupId
        this.wptTestID = fetchJob.wptTestId
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
        return "WPTDetailResult{" +
                "eventName='" + eventName + '\'' +
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
