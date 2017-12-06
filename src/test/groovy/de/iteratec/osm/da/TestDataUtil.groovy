package de.iteratec.osm.da

import de.iteratec.osm.da.asset.AssetRequest
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.wpt.LoadPhase
import de.iteratec.osm.da.wpt.data.Request
import de.iteratec.osm.da.wpt.data.Step
import de.iteratec.osm.da.wpt.data.WptDetailResult

class TestDataUtil {
    static AssetRequestGroup createAssetRequestGroup() {
        new AssetRequestGroup(
                dateOfPersistence: new Date(),
                osmInstance: 1,
                eventName:"Homepage_JuicyShop",
                measuredEvent:1,
                page:1,
                jobId:1,
                jobGroup:1,
                location:1,
                browser:1,
                epochTimeStarted:1133,
                mediaType:"Image",
                isFirstViewInStep:true,
                wptBaseUrl:"http://wpt.test.url.de",
                wptTestId:"121212_9R_q0",
                assets:[createAssetRequest()]
        ).save(failOnError:true, flush:true)
    }

    static def createAssetRequest() {
        new AssetRequest(bytesIn:1002,
                bytesOut:183,
                connectTimeMs:523,
                downloadTimeMs:334,
                loadTimeMs:2223,
                timeToFirstByteMs:1234,
                indexWithinHar:1,
                sslNegotiationTimeMs:72,
                dnsMs:123123,
                host:"werbeserver1.de",
                url:"/mein/werbe/banner.jpg?mit=mehreren&unterschiedlichen=parametern",
                mediaType: "image",
                subtype: "jpeg",
                urlWithoutParams:"/mein/werbe/banner.jpg",
                startPhase: LoadPhase.DomTime,
                endPhase: LoadPhase.DomTime
        ).save(failOnError:true, flush:true)
    }

    static OsmInstance createOsmInstance(){return new OsmInstance(name: "TestInstance",url:"http://demo.openspeedmonitor.de").save(failOnError:true, flush:true)}

    /**
     * Creates a Result, of a test of 2 runs, with 2 steps. In Every step there will be a mimimum of one html and one jpg loaded.
     * No other mimetype will be used and every run is without a cached step.
     * @param fetchJob
     * @return
     */
    static WptDetailResult createResultWith2Runs(FetchJob fetchJob){
        WptDetailResult result = new WptDetailResult(fetchJob)
        result.osmInstance = fetchJob.osmInstance
        result.browser = "FF"
        result.bandwidthDown = 200
        result.bandwidthUp = 300
        result.latency = 2
        result.packagelossrate = 1
        result.jobGroupID = fetchJob.jobGroupId
        result.location = "location:$result.browser"
        result.wptBaseUrl = fetchJob.wptBaseURL
        result.wptTestID = fetchJob.wptTestId[0]

        List<Step> steps = []
        def step = new Step(run: 1, stepNumber: 1, url: "http://openspeedmonitor.org", eventName: "osm", isFirstView: true, domTime: 900, loadTime: 1000, fullyLoaded: 3000)
        List<Request> requests = []
        requests << new Request(host: "openspeedmonitor.org",url: "/index.html", loadMs: 300, connectTimeMs: 30,
                downloadMs: 100,ttfbMs: 70,loadStart: 10,bytesIn: 100,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "text/html", indexWithinStep: 1)
        requests << new Request(host: "openspeedmonitor.org",url: "/picture.jpg", loadMs: 400, connectTimeMs: 30,
                downloadMs: 200,ttfbMs: 70,loadStart: 310,bytesIn: 300,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "image/jpg", indexWithinStep: 2)
        step.requests = requests
        steps << step

        step = new Step(run: 1, stepNumber: 2, url: "http://openspeedmonitor.org/path", eventName: "osm", isFirstView: true, domTime: 900, loadTime: 1000, fullyLoaded: 3000)
        requests = []
        requests << new Request(host: "openspeedmonitor.org",url: "/path/index.html", loadMs: 300, connectTimeMs: 30,
                downloadMs: 100,ttfbMs: 70,loadStart: 10,bytesIn: 100,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "text/html", indexWithinStep: 1)
        requests << new Request(host: "openspeedmonitor.org",url: "/picture2.jpg", loadMs: 400, connectTimeMs: 30,
                downloadMs: 200,ttfbMs: 70,loadStart: 310,bytesIn: 300,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "image/jpg", indexWithinStep: 2)
        requests << new Request(host: "openspeedmonitor.org",url: "/picture3.jpg", loadMs: 400, connectTimeMs: 30,
                downloadMs: 200,ttfbMs: 70,loadStart: 310,bytesIn: 300,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "image/jpg", indexWithinStep: 2)
        step.requests = requests
        steps << step

        step = new Step(run: 2, stepNumber: 1, url: "http://openspeedmonitor.org", eventName: "osm", isFirstView: true, domTime: 1000, loadTime: 1100, fullyLoaded: 3000)
        requests = []
        requests << new Request(host: "openspeedmonitor.org",url: "/index.html", loadMs: 300, connectTimeMs: 30,
                downloadMs: 100,ttfbMs: 70,loadStart: 10,bytesIn: 100,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "text/html", indexWithinStep: 1)
        requests << new Request(host: "openspeedmonitor.org",url: "/picture.jpg", loadMs: 400, connectTimeMs: 30,
                downloadMs: 200,ttfbMs: 70,loadStart: 310,bytesIn: 300,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "image/jpg", indexWithinStep: 2)
        step.requests = requests
        steps << step

        step = new Step(run: 2, stepNumber: 2, url: "http://openspeedmonitor.org/path", eventName: "osm", isFirstView: true, domTime: 1000, loadTime: 1100, fullyLoaded: 3000)
        requests = []
        requests << new Request(host: "openspeedmonitor.org",url: "/path/index.html", loadMs: 300, connectTimeMs: 30,
                downloadMs: 100,ttfbMs: 70,loadStart: 10,bytesIn: 100,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "text/html", indexWithinStep: 1)
        requests << new Request(host: "openspeedmonitor.org",url: "/picture2.jpg", loadMs: 400, connectTimeMs: 30,
                downloadMs: 200,ttfbMs: 70,loadStart: 310,bytesIn: 300,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "image/jpg", indexWithinStep: 2)
        requests << new Request(host: "openspeedmonitor.org",url: "/picture3.jpg", loadMs: 400, connectTimeMs: 30,
                downloadMs: 200,ttfbMs: 70,loadStart: 310,bytesIn: 300,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "image/jpg", indexWithinStep: 2)
        step.requests = requests
        steps << step

        result.steps = steps
        return result
    }

    static Step createStep(int domTime, int stepNumber, int run){
        def step = new Step(run: run, stepNumber: stepNumber, url: "http://openspeedmonitor.org", eventName: "osm", isFirstView: true, domTime: domTime, loadTime: 1000, fullyLoaded: 3000)
        List<Request> requests = []
        requests << new Request(host: "openspeedmonitor.org",url: "/index.html", loadMs: 300, connectTimeMs: 30,
                downloadMs: 100,ttfbMs: 70,loadStart: 10,bytesIn: 100,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "text/html", indexWithinStep: 1)
        requests << new Request(host: "openspeedmonitor.org",url: "/picture.jpg", loadMs: 400, connectTimeMs: 30,
                downloadMs: 200,ttfbMs: 70,loadStart: 310,bytesIn: 300,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "image/jpg", indexWithinStep: 2)
        step.requests = requests
        return step
    }


    static countAssetsInWPTDetailResult(WptDetailResult wptDetailResult){
        int count = 0
        wptDetailResult.steps.each {
            it.requests.each {
                count++
            }
        }
        return count
    }

    static countAssetsInAssetResourceGroup(List<AssetRequestGroup> groups){
        int count = 0
        groups.each {group ->
            group.assets.each {
                count++
            }
        }
        return count
    }

}
