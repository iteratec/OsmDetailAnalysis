package de.iteratec.osm.da.mapping

import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.instances.OsmInstance
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import groovy.mock.interceptor.StubFor
import spock.lang.Specification

@TestFor(MappingService)
@Mock([OsmInstance])
class MappingServiceTest extends Specification {

    def "Test update of one mapping"() {
        given:
        def browser = "FFNew"
        OsmInstance instance = createInstance()

        when:
        service.updateMapping(instance,new MappingUpdate(domain: OsmDomain.Browser,update:[1L:browser]))

        then:
        instance.getMapping(OsmDomain.Browser).mapping."1" == browser
    }

    def "Test update mapping with empty update map shouldn't change the domain"(){
        given:
        OsmInstance instance = createInstance()
        def browserClone = instance.getMapping(OsmDomain.Browser).mapping.clone()
        def locationClone = instance.getMapping(OsmDomain.Location).mapping.clone()
        def measuredEventClone = instance.getMapping(OsmDomain.MeasuredEvent).mapping.clone()
        def jobGroupClone = instance.getMapping(OsmDomain.JobGroup).mapping.clone()

        when:
        service.updateMapping(instance,new MappingUpdate(domain: OsmDomain.Browser,update: [:]))

        then:
        instance.getMapping(OsmDomain.Browser).mapping == browserClone
        instance.getMapping(OsmDomain.Location).mapping == locationClone
        instance.getMapping(OsmDomain.MeasuredEvent).mapping == measuredEventClone
        instance.getMapping(OsmDomain.JobGroup).mapping == jobGroupClone
    }

    def "Don't fetch, if all mappings exists within instance"() {
        given:
        boolean httpCall = false
        def stub = new StubFor(HttpRequestService)
        stub.demand.getJsonResponse {String baseUrl, String path, Map queryParams ->
            httpCall = true
        }
        service.httpRequestService = stub.proxyInstance()
        OsmInstance instance = createInstance()
        Map<OsmDomain, List<Long>> neededIds = [:]
        neededIds[OsmDomain.Browser] = [1L]
        neededIds[OsmDomain.JobGroup] = [1L]
        neededIds[OsmDomain.Location] = [1L]
        neededIds[OsmDomain.MeasuredEvent] = [1L]
        when:
        service.updateIfIdMappingsDoesntExist(instance.id,neededIds)
        then:
        !httpCall

    }

    def "GetOSMInstanceId"() {
        given:
        createInstance()

        when:
        def id = service.getOSMInstanceId("localhost:8080/")

        then:
        id > -1
    }


    def "GetNameForBrowserId"() {
        given:
        OsmInstance instance = createInstance()

        when:
        String name = service.getNameForBrowserId(instance.id, 1)

        then:
        name == "FF"
    }

    def "GetNameForLocationId"() {
        given:
        OsmInstance instance = createInstance()

        when:
        String name = service.getNameForLocationId(instance.id, 1)

        then:
        name == "Location"
    }

    def "GetNameForJobGroupId"() {
        given:
        OsmInstance instance = createInstance()

        when:
        String name = service.getNameForJobGroupId(instance.id, 1)

        then:
        name == "JG"

    }

    def "GetNameForMeasuredEventId"() {
        given:
        OsmInstance instance = createInstance()

        when:
        String name = service.getNameForMeasuredEventId(instance.id, 1)

        then:
        name == "ME"
    }

    def "GetNameForPageId"() {
        given:
        OsmInstance instance = createInstance()

        when:
        String name = service.getNameForPageId(instance.id, 1)

        then:
        name == "Page"
    }


    def "GetIdForJobGroupName"() {
        given:
        OsmInstance instance = createInstance()

        when:
        Long id = service.getIdForJobGroupName(instance.id, "JG")

        then:
        id == 1L
    }

    def "GetIdForBrowserName"() {
        given:
        OsmInstance instance = createInstance()

        when:
        Long id = service.getIdForBrowserName(instance.id, "FF")

        then:
        id == 1L
    }

    def "GetIdForLocationName"() {
        given:
        OsmInstance instance = createInstance()

        when:
        Long id = service.getIdForLocationName(instance.id, "Location")

        then:
        id == 1L
    }

    def "GetIdForMeasuredEventName"() {
        given:
        OsmInstance instance = createInstance()

        when:
        Long id = service.getIdForMeasuredEventName(instance.id, "ME")

        then:
        id == 1L
    }

    def "GetIdForPageName"() {
        given:
        OsmInstance instance = createInstance()

        when:
        Long id = service.getIdForPageName(instance.id, "Page")

        then:
        id == 1L
    }

    def "getBrowserMappings" () {
        given:
        OsmInstance instance = createInstance()

        when:
        Map browserMappings = service.getBrowserMappings(instance)

        then:
        browserMappings.size() == 2
        browserMappings["1"] == "FF"
        browserMappings["2"] == "Chrome"
    }

    def "getJobMappings" () {
        given:
        OsmInstance instance = createInstance()

        when:
        Map browserMappings = service.getJobMappings(instance)

        then:
        browserMappings.size() == 2
        browserMappings["1"] == "Job 1"
        browserMappings["2"] == "Job 2"
    }

    def "Test get IdUpdate with one domain"(){
        given:
        service.httpRequestService = Stub(HttpRequestService){
            getJsonResponse(_, _, _) >> new JsonSlurper().parseText(
                '{"target":{"Job":{"1":"TestJob","2":"AnotherTestJob"}}}'
            )
        }
        Map<String,List<Long>> neededUpdates = ["${OsmDomain.Job}":[1],
                                                "${OsmDomain.Job}":[2]]
        OsmInstance instance = createInstance()

        when:
        List<MappingUpdate> update = service.getIdUpdate(neededUpdates, instance)

        then: "There should be 2 updates for the Job Domain"
        update != null
        update.size() == 1
        update[0].domain == OsmDomain.Job
        update[0].updateCount() == 2
    }

    def "Test get IdUpdate with multiple domain"(){
        given:
        service.httpRequestService = Stub(HttpRequestService){
            getJsonResponse(_, _, _) >> new JsonSlurper().parseText(
                    '{"target":{"Job":{"1":"Chrome_Testsuite_Multistep","2":"Firefox_Testsuite_Multistep"},"JobGroup":{"1":"undefined","2":"perf-test-suite"}}}'
            )
        }
        Map<String,List<Long>> neededUpdates = [
            "${OsmDomain.Job}":[1,2],
            "${OsmDomain.JobGroup}":[1,2]
        ]
        OsmInstance instance = createInstance()

        when:
        def update = service.getIdUpdate(neededUpdates, instance)

        then: "There should be an update for each domain, as long as the osm got them"
        update != null
        update.size() == 2
        update[0].domain == OsmDomain.Job
        update[0].updateCount() == 2

        update[1].domain == OsmDomain.JobGroup
        update[1].updateCount() == 2
    }

    private createInstance = { ->
        def instance = new OsmInstance(url: "http://localhost:8080")
        instance.browserMapping.mapping."1" = "FF"
        instance.browserMapping.mapping."2" = "Chrome"
        instance.locationMapping.mapping."1" = "Location"
        instance.measuredEventMapping.mapping."1" = "ME"
        instance.jobGroupMapping.mapping."1" = "JG"
        instance.pageMapping.mapping."1" = "Page"
        instance.jobMapping.mapping."1" = "Job 1"
        instance.jobMapping.mapping."2" = "Job 2"
        instance.save()
    }
}
