package de.iteratec.osm.da.mapping

import de.iteratec.osm.da.HTTPRequestService
import de.iteratec.osm.da.instances.OsmInstance
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
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
        service.updateMapping(instance,OsmDomain.Browser,[1L:browser])

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
        service.updateMapping(instance,OsmDomain.Browser,[:])

        then:
        instance.getMapping(OsmDomain.Browser).mapping == browserClone
        instance.getMapping(OsmDomain.Location).mapping == locationClone
        instance.getMapping(OsmDomain.MeasuredEvent).mapping == measuredEventClone
        instance.getMapping(OsmDomain.JobGroup).mapping == jobGroupClone
    }

    def "Don't fetch, if all mappings exists within instance"() {
        given:
        boolean httpCall = false
        def stub = new StubFor(HTTPRequestService)
        stub.demand.getJsonResponse {String baseUrl, String path, queryParams ->
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
        service.updateIfMappingsDoesntExist(instance,neededIds)
        then:
        !httpCall

    }

    def "GetOSMInstanceId"() {
        given:
        createInstance()

        when:
        def id = service.getOSMInstanceId("http://test.openspeedmonitor.org")

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

    private createInstance = { ->
        def instance = new OsmInstance(name: "TestInstance", url: "http://test.openspeedmonitor.org")
        instance.browserMapping.mapping."1" = "FF"
        instance.locationMapping.mapping."1" = "Location"
        instance.measuredEventMapping.mapping."1" = "ME"
        instance.jobGroupMapping.mapping."1" = "JG"
        instance.pageMapping.mapping."1" = "Page"
        instance.save()
    }
}
