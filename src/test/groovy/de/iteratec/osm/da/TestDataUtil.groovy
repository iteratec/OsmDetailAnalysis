package de.iteratec.osm.da

import de.iteratec.oms.da.external.mapping.OSMDomain
import de.iteratec.osm.da.external.wpt.data.Request
import de.iteratec.osm.da.external.wpt.data.Step
import de.iteratec.osm.da.external.wpt.data.WptDetailResult
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.external.FetchJob
import de.iteratec.osm.da.external.instances.OsmInstance
import de.iteratec.osm.da.external.instances.OsmMapping


class TestDataUtil {
    static OsmInstance createOsmInstance(){

        def instance = new OsmInstance(name: "TestInstance",url:"http://demo.openspeedmonitor.de")
        instance.osmMappings."$OSMDomain.MeasuredEvent" = new OsmMapping(mapping: [1l:"ME:1"])
        return instance
    }

    /**
     * Creates a Result, of a test of 2 runs, with 2 steps. In Every step there will be a mimimum of one html and one jpg loaded.
     * No other mimetype will be used and every run is without a cached step.
     * @param fetchJob
     * @return
     */
    static WptDetailResult createResult(FetchJob fetchJob){
        WptDetailResult result = new WptDetailResult(fetchJob)
        result.osmInstance = fetchJob.osmInstance
        result.browser = "FF"
        result.bandwidthDown = 200
        result.bandwidthUp = 300
        result.latency = 2
        result.packagelossrate = 1
        result.epochTimeCompleted = 1467969368
        result.jobGroupID = fetchJob.jobGroupId
        result.location = "location:$result.browser"
        result.wptBaseUrl = fetchJob.wptBaseURL
        result.wptTestID = fetchJob.wptTestId[0]

        List<Step> steps = []
        def step = new Step(run: 1, step: 1, docTime: 1000, url: "http://openspeedmonitor.org", eventName: "osm", isFirstView: true)
        List<Request> requests = []
        requests << new Request(host: "openspeedmonitor.org",url: "/index.html", loadMs: 300, connectTimeMs: 30,
                downloadMs: 100,ttfbMs: 70,loadStart: 10,bytesIn: 100,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "text/html", indexWithinStep: 1)
        requests << new Request(host: "openspeedmonitor.org",url: "/picture.jpg", loadMs: 400, connectTimeMs: 30,
                downloadMs: 200,ttfbMs: 70,loadStart: 310,bytesIn: 300,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "image/jpg", indexWithinStep: 2)
        step.requests = requests
        steps << step

        step = new Step(run: 1, step: 2, docTime: 1000, url: "http://openspeedmonitor.org/path", eventName: "osm", isFirstView: true)
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

        step = new Step(run: 2, step: 1, docTime: 1000, url: "http://openspeedmonitor.org", eventName: "osm", isFirstView: true)
        requests = []
        requests << new Request(host: "openspeedmonitor.org",url: "/index.html", loadMs: 300, connectTimeMs: 30,
                downloadMs: 100,ttfbMs: 70,loadStart: 10,bytesIn: 100,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "text/html", indexWithinStep: 1)
        requests << new Request(host: "openspeedmonitor.org",url: "/picture.jpg", loadMs: 400, connectTimeMs: 30,
                downloadMs: 200,ttfbMs: 70,loadStart: 310,bytesIn: 300,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "image/jpg", indexWithinStep: 2)
        step.requests = requests
        steps << step

        step = new Step(run: 2, step: 2, docTime: 1000, url: "http://openspeedmonitor.org/path", eventName: "osm", isFirstView: true)
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
