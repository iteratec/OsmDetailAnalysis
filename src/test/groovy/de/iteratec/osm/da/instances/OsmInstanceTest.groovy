package de.iteratec.osm.da.instances

import spock.lang.Specification
import spock.lang.Unroll

class OsmInstanceTest extends Specification {

    @Unroll("Test that a OsmInstance with url #givenUrl will return the url #expectedUrl")
    def "Test URLs with trailing slash"(){
        given:
        expect:
        new OsmInstance(url:givenUrl).url == expectedUrl

        where:
        givenUrl                      | expectedUrl
        "localhost"                   | "localhost"
        "localhost"                   | "localhost"
        "localhost/path/"             | "localhost/path"
        "http://localhost"            | "http://localhost"
        "http://localhost/"           | "http://localhost"
        "http://localhost/path"       | "http://localhost/path"
        "http://localhost/path/"      | "http://localhost/path"
    }
}
