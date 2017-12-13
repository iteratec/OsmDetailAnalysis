package de.iteratec.osm.da.instances

import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

@Mock([OsmInstance])
class OsmInstanceTest extends Specification {

    @Unroll("Test that a OsmInstance with url #givenUrl will return the url #expectedUrl")
    def "Test URLs with trailing slash"() {
        given:
        def instance = new OsmInstance(url: givenUrl).save(failOnError: true)

        expect: "url has trailing slash after saving"
        instance.domainPath == expectedUrl

        where:
        givenUrl                 | expectedUrl
        "localhost"              | "localhost/"
        "localhost/"             | "localhost/"
        "localhost/path/"        | "localhost/path/"
    }

    def "Test url setter"(){
        given:
        String url = "http://test.de"

        when:
        OsmInstance instance = new OsmInstance(url: url)

        then:
        instance.domainPath == "test.de/"
        instance.protocol == "http"
        instance.url == url+"/"
    }
}
