package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.TestDataUtil
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.instances.OsmMapping
import de.iteratec.osm.da.mapping.MappingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(WptDetailResultConvertService)
@Mock([OsmInstance, FetchJob, OsmMapping])
class WptDetailResultConvertServiceTest extends Specification {

    def setup(){
        mockService()
    }

    def "test amount of asset groups resulting from a WPTDetailResult"(){
        given:
        OsmInstance instance = new TestDataUtil().createOsmInstance().save()
        FetchJob fetchJob = new FetchJob(osmInstance: instance.id,wptBaseURL: "http://wptTest.openspeedmonitor.org", wptTestId: ["163648_BD_4"], wptVersion:"2.19", jobGroupId: 1)
        def result = TestDataUtil.createResultWith2Runs(fetchJob)
        when:
        def list = service.convertWPTDetailResultToAssetGroups(result,fetchJob)
        then: "There should be a group for every media type, of every event in a step"
        list.size() == 8
    }

    def "test that all resources are present after converting"(){
        given:
        OsmInstance instance = new TestDataUtil().createOsmInstance().save()
        FetchJob fetchJob = new FetchJob(osmInstance: instance.id,wptBaseURL: "http://wptTest.openspeedmonitor.org", wptTestId: ["163648_BD_4"], jobGroupId: 1)
        def result = TestDataUtil.createResultWith2Runs(fetchJob)
        def assetCount = TestDataUtil.countAssetsInWPTDetailResult(result)

        when:
        def list = service.convertWPTDetailResultToAssetGroups(result,fetchJob)

        then: "There should be a group for every media type, of every event in a step"
        TestDataUtil.countAssetsInAssetResourceGroup(list) == assetCount
    }

    @Unroll("Test that the pagename of the eventname #eventName should be #expectedName")
    def "test that the page will be correct parsed"(){
        given:
        expect:
        service.getPageName(eventName) == expectedName

        where:
        eventName               | expectedName
        "WK:::test"             | "WK"
        "WK:::testStuff:::3"    | "undefined"
        "WK::testStuff"         | "undefined"
        "WK:testStuff"          | "undefined"
        "HP_ENTRY:::test"       | "HP_ENTRY"
        "test"                  | "undefined"
    }

    def mockService(){
        def mappingService = Mock(MappingService)
        mappingService.getIdForMeasuredEventName(_,_) >> {1}
        mappingService.getIdForBrowserName(_,_) >> {1}
        mappingService.getIdForLocationName(_,_) >> {1}
        service.mappingService = mappingService
    }

}
