package de.iteratec.osm.da.wpt.data

import de.iteratec.osm.da.TestDataUtil
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.instances.OsmMapping
import de.iteratec.osm.da.wpt.LoadPhase
import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

@Mock([OsmInstance, FetchJob, OsmMapping])
class RequestTest extends Specification {
    @Unroll("Request with start #start and end #end should be in startphase #startPhase and endphase #endPhase")
    def "SetPhase"(){
        given:
        OsmInstance instance = new TestDataUtil().createOsmInstance().save(failOnError:true)
        FetchJob fetchJob = new FetchJob(osmInstance: instance.id,wptBaseURL: "http://wptTest.openspeedmonitor.org", wptTestId: ["163648_BD_4"], jobGroupId: 1)
        WptDetailResult result = TestDataUtil.createResultWith2Runs(fetchJob)
        result.steps[0].domTime = 100
        result.steps[0].loadTime = 200
        result.steps[0].fullyLoaded = 300

        expect:

        Request request = new Request(host: "openspeedmonitor.org",url: "/index.html", loadMs: 300, connectTimeMs: 30,
                downloadMs: 100,ttfbMs: 70,loadStart: start, loadEnd: end, bytesIn: 100,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "text/html", indexWithinStep: 1)
        result.steps[0].requests << request

        result.calculateAdditionalInformations()
        request.startPhase == startPhase
        request.endPhase == endPhase

        where:
        start   | end   | startPhase            | endPhase
        50      |  50   | LoadPhase.DomTime     | LoadPhase.DomTime
        100     | 100   | LoadPhase.DomTime     | LoadPhase.DomTime
        100     | 101   | LoadPhase.DomTime     | LoadPhase.LoadTime
        101     | 101   | LoadPhase.LoadTime    | LoadPhase.LoadTime
        200     | 200   | LoadPhase.LoadTime    | LoadPhase.LoadTime
        200     | 201   | LoadPhase.LoadTime    | LoadPhase.FullyLoaded
        201     | 201   | LoadPhase.FullyLoaded | LoadPhase.FullyLoaded
        9999    | 99999 | LoadPhase.FullyLoaded | LoadPhase.FullyLoaded

    }


}
