package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.fetch.FetchFailReason
import de.iteratec.osm.da.wpt.data.Request
import de.iteratec.osm.da.wpt.data.Step
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.resolve.exceptions.WptNotAvailableException
import de.iteratec.osm.da.wpt.resolve.exceptions.WptResultMissingValueException
import de.iteratec.osm.da.wpt.resolve.exceptions.WptTestIdDoesntExistException
import de.iteratec.osm.da.wpt.resolve.exceptions.WptTestWasCancelledException
import de.iteratec.osm.da.wpt.resolve.exceptions.WptTestWasEmptyException
import grails.util.Holders
import groovyx.net.http.ResponseParseException
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * Use this strategy for versions >= 2.19
 */
class WptDetailDataDefaultStrategy implements WptDetailDataStrategyI{

    HttpRequestService httpRequestService
    static WPTVersion minimumVersion = WPTVersion.get("2.19")
    private static final log = LogFactory.getLog(this)

    @Override
    WptDetailResult getResult(FetchJob fetchJob) {

        def jsonResponse = loadJson(fetchJob)
        return createResult(fetchJob, jsonResponse)
    }

    def loadJson(FetchJob fetchJob, int tries = 0){
        //We set the multiStepFormat, because the new version can delivery single steps with the same format as the multi step results.
        try{
            return httpRequestService.getJsonResponse(fetchJob.wptBaseURL, "jsonResult.php", [test:fetchJob.wptTestId,requests: 1, multiStepFormat:1])
        } catch (ResponseParseException e){
            if(tries<3){
                return loadJson(fetchJob, ++tries)
            } else{
                throw new WptNotAvailableException(fetchJob.wptBaseURL)
            }
        }
    }

    static private WptDetailResult createResult(FetchJob fetchJob, def jsonResponse){
        if(jsonResponse.statusCode == 400) throw new WptTestIdDoesntExistException(fetchJob.wptTestId,fetchJob.wptBaseURL)
        WptDetailResult result = new WptDetailResult(fetchJob)
        result.location = jsonResponse.data.location
        def locationSplit = (jsonResponse.data.location as String).split(":")
        if(locationSplit.size() > 1) result.browser = locationSplit[1]
        setConnectivity(result,jsonResponse)
        setSteps(result,fetchJob,jsonResponse)
        result.calculateAdditionalInformations()
        if(!result.hasAllValues()) throw new WptResultMissingValueException()
        return result
    }

    static private void setConnectivity(WptDetailResult result, def json){
        result.bandwidthDown = convertIntValue json.data.bwDown
        result.bandwidthUp = convertIntValue json.data.bwUp
        result.latency = convertIntValue json.data.latency
        result.packagelossrate = convertIntValue json.data.plr
    }

    static private void setSteps(WptDetailResult result, FetchJob fetchJob, def json) throws WptTestWasEmptyException{
        List<Step> steps = []
        if(json.statusText == "Test Cancelled"){
            throw new WptTestWasCancelledException(fetchJob.wptTestId, fetchJob.wptBaseURL)
        }
        json.data.runs.each{def run ->
            run.value?.firstView?.steps?.each{
                Step step = createStep(it)
                if(step){
                    step.isFirstView = true
                    steps << step
                }
            }
            run.value?.secondView?.steps?.each{
                Step step = createStep(it)
                if(step){
                    step.isFirstView = false
                    steps << step
                }
            }
        }
        if(steps.isEmpty()){
            throw new WptTestWasEmptyException(fetchJob.wptTestId, fetchJob.wptBaseURL)
        }
        result.steps = steps
    }

    /**
     *
     * @param stepInJason
     * @return a Step or null if there was no data available
     */
    static private Step createStep(def stepInJason){
        if(!stepInJason) return null
        Step step = new Step()
        step.epochTimeStarted = stepInJason.date
        step.domTime = stepInJason.docTime
        step.loadTime = stepInJason.loadTime
        step.fullyLoaded = stepInJason.fullyLoaded
        step.run = stepInJason.run
        step.stepNumber = stepInJason.step
        step.eventName = stepInJason.eventName
        step.url = stepInJason.URL
        step.requests = createRequests(stepInJason.requests)
        if(!step.hasMetaValues() && !step.hasRequests()) return null
        return step
    }

    static private List<Request> createRequests(def requestMap){
        List<Request> requests = []
        requestMap?.each {
            Request request = new Request()
            request.bytesIn = convertIntValue(it.bytesIn)
            request.bytesOut = convertIntValue(it.bytesOut)
            request.indexWithinStep = convertIntValue(it.index)
            request.ttfbMs = convertIntValue(it.ttfb_ms) 
            request.loadStart = convertIntValue(it.load_start) 
            request.loadEnd = convertIntValue(it.load_end) 
            request.loadMs = convertIntValue(it.load_ms) 
            request.host = it.host
            request.url = it.url
            request.sslNegotiationTimeMs = convertIntValue(it.ssl_ms) 
            request.connectTimeMs = convertIntValue(it.connect_ms) 
            request.downloadMs = convertIntValue(it.download_ms) 
            request.contentType = it.contentType
            request.dnsTimeMs = convertIntValue(it.dns_ms)
            if(!request.hasValues()) throw new WptResultMissingValueException()
            requests << request
        }
        return requests
    }

    /**
     * Cast a String to an int, if this object is not already an int.
     * This method ist used, because the wpt results are inconsistent and sometimes the numbers
     * are string and sometimes they are ints.
     * @param value
     * @return
     */
    private static int convertIntValue(def value){
        if(value instanceof Integer) return value
        if(value instanceof String){
            return value as int
        }
        return -1
    }

    @Override
    boolean compatibleWithVersion(WPTVersion version) {
        return minimumVersion <= version
    }

}
