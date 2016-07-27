package de.iteratec.osm.da.wpt.data

import de.iteratec.osm.da.TestDataUtil
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.instances.OsmMapping
import grails.test.mixin.Mock
import spock.lang.Specification

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
        result.steps.find{it.stepNumber == 1 && it.isMedian}.docTime == 900
        result.steps.find{it.stepNumber == 2 && it.isMedian}.docTime == 950
    }
    def "MarkMedianRunsWith2Runs"() {
        given:
        OsmInstance instance = new TestDataUtil().createOsmInstance().save()
        FetchJob fetchJob = new FetchJob(osmInstance: instance.id,wptBaseURL: "http://wptTest.openspeedmonitor.org", wptTestId: ["163648_BD_4"], jobGroupId: 1)
        WptDetailResult result = TestDataUtil.createResultWith2Runs(fetchJob)

        when:
        result.markMedianRuns()
        then:
        result.steps.find{it.stepNumber == 1 && it.isMedian}.docTime == 900
        result.steps.find{it.stepNumber == 2 && it.isMedian}.docTime == 900
    }



}
