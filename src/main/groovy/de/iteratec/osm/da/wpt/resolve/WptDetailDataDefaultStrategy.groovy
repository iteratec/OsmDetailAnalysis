package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.data.Request
import de.iteratec.osm.da.wpt.data.Step
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.wpt.resolve.exceptions.*
import org.apache.commons.logging.LogFactory
/**
 * Use this strategy for versions >= 2.19
 */
class WptDetailDataDefaultStrategy implements WptDetailDataStrategyI{

    HttpRequestService httpRequestService
    static WPTVersion minimumVersion = WPTVersion.get("2.19")
    private static final log = LogFactory.getLog(this)

    @Override
    WptDetailResult getResult(FetchJob fetchJob) {

        log.debug("Start loading of JsonResult for fetchJob=${fetchJob.id}.")
        def jsonResponse = loadJson(fetchJob)
        log.debug("Finished loading of JsonResult for fetchJob=${fetchJob.id}. Size is ${jsonResponse.size()}. Starting to createResult")
        return createResult(fetchJob, jsonResponse)
    }

    def loadJson(FetchJob fetchJob, int tries = 0){
        //We set the multiStepFormat, because the new version can delivery single steps with the same format as the multi step results.
        try{
            log.debug("Attempt ${tries +1} to load JsonResponse from WptServer=${fetchJob.wptBaseURL} WptTestId=${fetchJob.wptTestId}")
            return httpRequestService.getJsonResponse(
                fetchJob.wptBaseURL,
                "/jsonResult.php",
                [test: fetchJob.wptTestId,requests: 1, multistepFormat: 1]
            )
        } catch (Exception e){
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
        ensurePresenceOfAllValues(result)
        log.debug("Finished to createResult for fetchJob=${fetchJob.id}.")
        return result
    }

    static private  ensurePresenceOfAllValues(WptDetailResult result){
        def missingValues = [:]
        if (!(result.bandwidthDown >= 0))missingValues["bandwidthDown"]= result.bandwidthDown
        if (!(result.bandwidthUp >= 0))missingValues["bandwidthUp"]= result.bandwidthUp
        if (!(result.latency >= 0))missingValues["latency"]= result.latency
        if (!(result.packagelossrate >= 0))missingValues["packagelossrate"]= result.packagelossrate
        if (!(result.location))missingValues["location"]= result.location
        if (!(result.browser))missingValues["browser"]= result.browser
        if (!(result.jobGroupID >= 0))missingValues["jobGroupID"]= result.jobGroupID
        if (!(result.wptBaseUrl))missingValues["wptBaseUrl"]= result.wptBaseUrl
        if (!(result.wptTestID))missingValues["wptTestID"]= result.wptTestID
        if (!(result.osmInstance >= 0))missingValues["osmInstance"]= result.osmInstance
        if (!(result.jobId))missingValues["jobId"]= result.jobId

        def missingValueStringList = []
        missingValues.each {key, value ->
            missingValueStringList.add(key)
        }
        if (missingValueStringList){
            throw new WptResultMissingValueException(missingValueStringList)
        }
        return missingValues.isEmpty()
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
        log.debug("Start processing ${result}")
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
        log.debug("Start creating step with the following parameters: epochTimeStarted=${step.epochTimeStarted} domTime=${step.domTime} step.loadTime=${step.loadTime} fullyLoaded=${step.fullyLoaded} run=${step.run} stepNumber=${step.stepNumber} eventName=${step.eventName} url=${step.url}")
        step.requests = createRequests(stepInJason.requests)
        if(!step.hasMetaValues() && !step.hasRequests()) return null
        log.debug("Step created")
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
            def missingValuesStringList = []
            if(!request.host)missingValuesStringList.add("host")
            if(!request.url)missingValuesStringList.add("url")
            if(!(request.bytesOut >-1))missingValuesStringList.add("bytesOut")
            if(!(request.bytesIn >-1))missingValuesStringList.add("bytesIn")
            if(!(request.loadMs >-1))missingValuesStringList.add("loadMs")
            if(missingValuesStringList) {
                log.error("The following values are missing: ${missingValuesStringList} ${it}")
//                throw new WptResultMissingValueException(missingValuesStringList)
            }else {
                requests << request
            }
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
