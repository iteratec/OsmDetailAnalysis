package de.iteratec.osm.da.wpt.resolve

import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.wpt.data.Request
import de.iteratec.osm.da.wpt.data.WptDetailResult
import groovy.json.JsonSlurper
import spock.lang.Specification

class WptDetailDataDefaultStrategyTest extends Specification {

    static final String testId = "160810_A7_4D"
    static final String wptBaseUrl = "http://dev.server01.wpt.iteratec.de/"
    static final File WPT_RESULT_JSON = new File('src/test/resources/jsonResponses/devServer01_160810_A7_4D.json')

    def "Test result download"(){
        given: "A FetchJob"
        WptDetailDataDefaultStrategy strategy = new WptDetailDataDefaultStrategy(
            httpRequestService: Stub(HttpRequestService){
                    getJsonResponse(_, _, _) >> new JsonSlurper().parse(WPT_RESULT_JSON)
            }
        )
        FetchJob fetchJob = new FetchJob(osmInstance: 1,jobId: 2, jobGroupId: 3, wptBaseURL: wptBaseUrl, wptTestId: [testId], wptVersion:"2.19")

        when: "We want to download the given id from the fetchjob"
        WptDetailResult result = strategy.getResult(fetchJob)

        then: "We should receive a WptDetailResult, which got informations" //For correct informations see next tests
        result.steps.size() == 4
    }

    def "Test if downloaded meta informations are correct"(){
        given: "A FetchJob"
        WptDetailDataDefaultStrategy strategy = new WptDetailDataDefaultStrategy(
                httpRequestService: Stub(HttpRequestService){
                    getJsonResponse(_, _, _) >> new JsonSlurper().parse(WPT_RESULT_JSON)
                }
        )
        FetchJob fetchJob = new FetchJob(osmInstance: 1,jobId: 2, jobGroupId: 3, wptBaseURL: wptBaseUrl, wptTestId: [testId], wptVersion:"2.19")

        when: "We want to download the given id from the fetchjob"
        WptDetailResult result = strategy.getResult(fetchJob)

        then: "We should receive a WptDetailResult with correct informations"
        result.steps.size() == 4
        result.browser == "Chrome"
        result.location == "iteratec-dev-hetzner-win7:Chrome"
        result.osmInstance == 1
        result.jobId == 2
        result.jobGroupID == 3
        result.bandwidthDown == 6000
        result.bandwidthUp == 6000
        result.latency == 50
        result.wptTestID == testId
        result.wptBaseUrl == wptBaseUrl
    }

    def "Test if downloaded steps are correct"(){
        given: "A FetchJob"
        WptDetailDataDefaultStrategy strategy = new WptDetailDataDefaultStrategy(
                httpRequestService: Stub(HttpRequestService){
                    getJsonResponse(_, _, _) >> new JsonSlurper().parse(WPT_RESULT_JSON)
                }
        )
        FetchJob fetchJob = new FetchJob(osmInstance: 1,jobId: 2, jobGroupId: 3, wptBaseURL: wptBaseUrl, wptTestId: [testId], wptVersion:"2.19")

        when: "We want to download the given id from the fetchjob"
        WptDetailResult result = strategy.getResult(fetchJob)

        then: "We should receive a WptDetailResult with correct informations"
        result.steps.size() == 4
        //For the first step we do further investigations. The rest uses the same code and should be correct, if the first one was also correct.
        result.steps[0].requests.size() == 156
        //this are all contenttypes which are within the step of this result. So the difference should be the empty list

        List<Request> step1Requests = result.steps[0].requests
        (step1Requests*.contentType.toSet() - [null,"application/json", "text/html",
                                                         "image/vnd.microsoft.icon", "text/css", "application/javascript",
                                                         "image/gif", "text/javascript", "application/x-javascript",
                                                         "image/jpeg", "image/svg+xml", "font/woff", "text/plain",
                                                         "image/png"]).isEmpty()
        step1Requests[0].host == "www.esprit.de"
        step1Requests[0].url == "/"
        step1Requests[0].loadMs == 78
        step1Requests[0].connectTimeMs == 62
        step1Requests[0].downloadMs == 0
        step1Requests[0].ttfbMs == 78
        step1Requests[0].loadStart == 166
        step1Requests[0].loadEnd == 244
        step1Requests[0].bytesIn == 731
        step1Requests[0].bytesOut == 430
        step1Requests[0].sslNegotiationTimeMs == -1
        step1Requests[0].contentType == "text/html"
        step1Requests[0].indexWithinStep == 0
        step1Requests[0].dnsTimeMs == -1
        result.steps[1].requests.size() == 17
        result.steps[2].requests.size() == 0
        result.steps[3].requests.size() == 0
    }
}
