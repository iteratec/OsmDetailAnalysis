package de.iteratec.osm.da.instances

import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

@Mock([OsmInstance])
class OsmInstanceTest extends Specification {

    @Unroll("Test that a OsmInstance with url #givenUrl will return the url #expectedUrl")
    def "Test URLs with trailing slash"() {
        given:
        String instanceName = "testInstance"

        when:
        new OsmInstance(name: instanceName, url: givenUrl).save(failOnError: true)

        then: "url has trailing slash after saving"
        OsmInstance.findByName(instanceName).url == expectedUrl

        where:
        givenUrl                 | expectedUrl
        "localhost"              | "localhost/"
        "localhost/"             | "localhost/"
        "localhost/path/"        | "localhost/path/"
        "http://localhost"       | "http://localhost/"
        "http://localhost/"      | "http://localhost/"
        "http://localhost/path"  | "http://localhost/path/"
        "http://localhost/path/" | "http://localhost/path/"
    }
}
