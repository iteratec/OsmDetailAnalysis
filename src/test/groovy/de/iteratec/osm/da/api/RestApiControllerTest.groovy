package de.iteratec.osm.da.api

import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(RestApiController)
class RestApiControllerTest extends Specification {

    void 'test valid command object'() {
        //We only test if the url will be edited, if there is a trailing slash. The full test for this functionality is tested in OsmInstanceTest
        when:
        def command = new OsmCommand(osmUrl: 'localhost/')

        then:
        command.osmUrl == "localhost"
    }
}
