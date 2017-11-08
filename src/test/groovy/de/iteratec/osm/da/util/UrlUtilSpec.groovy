package de.iteratec.osm.da.util

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Unroll
class UrlUtilSpec extends Specification {

    void "trailing slash is appended correctly: #url -> #expectedOutcome"() {
        expect:"append trailing slash to #url gives #expectedOutcome"
        UrlUtil.appendTrailingSlash(url) == expectedOutcome
        where:
        url                             | expectedOutcome
        'http://www.test.com'           | 'http://www.test.com/'
        'http://www.test.com/'          | 'http://www.test.com/'
        'https://www.test.com'          | 'https://www.test.com/'
        'https://www.test.com/'         | 'https://www.test.com/'
        'https://www.test.com:8080'     | 'https://www.test.com:8080/'
        'https://www.test.com:8080/'    | 'https://www.test.com:8080/'
        'www.test.com'                  | 'www.test.com/'
        'www.test.com/'                 | 'www.test.com/'
    }

    void "hypertext protocols are removed correctly: #url -> #expectedOutcome"() {
        expect:"append trailing slash to #url gives #expectedOutcome"
        UrlUtil.removeHypertextProtocols(url) == expectedOutcome
        where:
        url                             | expectedOutcome
        'http://www.test.com'           | 'www.test.com'
        'https://www.test.com/'         | 'www.test.com/'
        'https://www.test.com:8080/'    | 'www.test.com:8080/'
        'www.test.com'                  | 'www.test.com'
    }

}