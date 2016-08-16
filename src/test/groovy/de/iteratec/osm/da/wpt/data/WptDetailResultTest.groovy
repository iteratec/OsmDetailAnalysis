package de.iteratec.osm.da.wpt.data

import de.iteratec.osm.da.TestDataUtil
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.instances.OsmMapping
import de.iteratec.osm.da.wpt.LoadPhase
import grails.test.mixin.Mock
import junit.framework.Test
import spock.lang.Specification
import spock.lang.Unroll

@Mock([OsmInstance, FetchJob, OsmMapping])
class WptDetailResultTest extends Specification {


    def "MarkMedianRunsWith3Runs"() {
        given:
        OsmInstance instance = new TestDataUtil().createOsmInstance().save()
        FetchJob fetchJob = new FetchJob(osmInstance: instance.id,wptBaseURL: "http://wptTest.openspeedmonitor.org", wptTestId: ["163648_BD_4"], jobGroupId: 1)
        WptDetailResult result = TestDataUtil.createResultWith2Runs(fetchJob)
        result.steps << TestDataUtil.createStep(800,1,3)
        result.steps << TestDataUtil.createStep(950,2,3)

        when:
        result.markMedianRuns()
        then:
        result.steps.find{it.stepNumber == 1 && it.isMedian}.domTime == 900
        result.steps.find{it.stepNumber == 2 && it.isMedian}.domTime == 950
    }
    def "MarkMedianRunsWith2Runs"() {
        given:
        OsmInstance instance = new TestDataUtil().createOsmInstance().save()
        FetchJob fetchJob = new FetchJob(osmInstance: instance.id,wptBaseURL: "http://wptTest.openspeedmonitor.org", wptTestId: ["163648_BD_4"], jobGroupId: 1)
        WptDetailResult result = TestDataUtil.createResultWith2Runs(fetchJob)

        when:
        result.markMedianRuns()
        then:
        result.steps.find{it.stepNumber == 1 && it.isMedian}.domTime == 900
        result.steps.find{it.stepNumber == 2 && it.isMedian}.domTime == 900
    }
    def "Set Request Phases"(){
        given:
        OsmInstance instance = new TestDataUtil().createOsmInstance().save(failOnError:true)
        FetchJob fetchJob = new FetchJob(osmInstance: instance.id,wptBaseURL: "http://wptTest.openspeedmonitor.org", wptTestId: ["163648_BD_4"], jobGroupId: 1)
        WptDetailResult result = TestDataUtil.createResultWith2Runs(fetchJob)
        result.steps[0].domTime = 100
        result.steps[0].loadTime = 200
        result.steps[0].fullyLoaded = 300
        Request request = new Request(host: "openspeedmonitor.org",url: "/index.html", loadMs: 300, connectTimeMs: 30,
                downloadMs: 100,ttfbMs: 70,loadStart: 100, loadEnd: 300, bytesIn: 100,bytesOut: 10,sslNegotiationTimeMs: 0,
                contentType: "text/html", indexWithinStep: 1)
        result.steps[0].requests << request
        boolean allStartPhasesMarked = true
        boolean allEndPhasesMarked = true

        when:
        result.calculateAdditionalInformations()

        then:
        result.steps.each {step ->
            step.requests.each {
                allStartPhasesMarked &= request.startPhase != null
                allEndPhasesMarked &= request.endPhase != null
            }
        }
        allEndPhasesMarked
        allStartPhasesMarked

    }
}
