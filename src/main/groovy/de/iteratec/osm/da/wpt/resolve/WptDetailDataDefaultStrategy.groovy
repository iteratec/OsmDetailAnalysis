package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.wpt.data.Request
import de.iteratec.osm.da.wpt.data.Step
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.HTTPRequestService
import de.iteratec.osm.da.fetch.FetchJob
import org.springframework.beans.factory.annotation.Autowired

/**
 * Use this strategy for versions >= 2.19
 */
class WptDetailDataDefaultStrategy implements WptDetailDataStrategyI{

    @Autowired
    HTTPRequestService httpRequestService
    static WPTVersion minimumVersion = WPTVersion.get("2.19")

    @Override
    WptDetailResult getResult(FetchJob fetchJob) {
        //We set the multiStepFormat, because the new version can delivery single steps with the same format as the multi step results.
        def jsonResponse = httpRequestService.getJsonResponse(fetchJob.wptBaseURL, "jsonResult.php", [test:fetchJob.currentId,requests: 1, multiStepFormat:1])
        return createResult(fetchJob, jsonResponse)
    }

    static private WptDetailResult createResult(FetchJob fetchJob, def jsonResponse){
        WptDetailResult result = new WptDetailResult(fetchJob)
        result.location = jsonResponse.data.location
        result.epochTimeCompleted = jsonResponse.data.completed as long
        def locationSplit = jsonResponse.data.location.split(":")
        if(locationSplit.size() > 1) result.browser = locationSplit[1]
        setConnectivity(result,jsonResponse)
        setSteps(result,jsonResponse)
        result.markMedianRuns()
        return result
    }

    static private void setConnectivity(WptDetailResult result, def json){
        result.bandwidthDown = json.data.bwDown
        result.bandwidthUp = json.data.bwUp
        result.latency = json.data.latency
        result.packagelossrate = Integer.parseInt(json.data.plr)
    }

    static private void setSteps(WptDetailResult result, def json){
        List<Step> steps = []
        json.data.runs.each{def run ->
            run.value?.firstView?.steps?.each{
                Step step = createStep(it)
                step.isFirstView = true
                steps << step
            }
            run.value?.secondView?.steps?.each{
                Step step = createStep(it)
                step.isFirstView = false
                steps << step
            }
        }
        result.steps = steps
    }

    static private Step createStep(def stepInJason){
        Step step = new Step()
        step.docTime = stepInJason.docTime
        step.run = stepInJason.run
        step.step = stepInJason.step
        step.eventName = stepInJason.eventName
        step.url = stepInJason.URL
        step.requests = createRequests(stepInJason.requests)
        return step
    }

    static private List<Request> createRequests(def requestMap){
        List<Request> requests = []
        requestMap?.each {
            Request request = new Request()
            request.bytesIn = it.bytesIn as int
            request.bytesOut = it.bytesIn as int
            request.indexWithinStep = it.bytesIn as int
            request.ttfbMs = it.bytesIn as int
            request.loadStart = it.bytesIn as int
            request.loadMs = it.bytesIn as int
            request.host = it.host
            request.url = it.url
            request.sslNegotiationTimeMs = it.ssl_ms as int
            request.connectTimeMs = it.connect_ms as int
            request.downloadMs = it.download_ms
            request.contentType = it.contentType
            requests << request
        }
        return requests
    }

    @Override
    boolean compatibleWithVersion(WPTVersion version) {
        return minimumVersion <= version
    }

}
