package de.iteratec.oms.da.external.wpt.data

import spock.lang.Specification
import spock.lang.Unroll


class WPTVersionTest extends Specification {

    @Unroll("Compare #leftVersion #name #rightVersion")
    def "CompareTo"() {
        expect:
        compare(leftVersion, rightVersion) == truth

        where:
        leftVersion             | rightVersion              |compare           |name  || truth
        WPTVersion.get("1.19")  | WPTVersion.get("1.19")    |{v1,v2->v1 == v2} |"eq"  || true
        WPTVersion.get("1.18")  | WPTVersion.get("1.20")    |{v1,v2->v1 == v2} |"eq"  || false
        WPTVersion.get("1.20")  | WPTVersion.get("1.19")    |{v1,v2->v1 == v2} |"eq"  || false

        WPTVersion.get("1.19")  | WPTVersion.get("1.19")    |{v1,v2->v1 > v2}  |"gt"  || false
        WPTVersion.get("1.19")  | WPTVersion.get("1.18")    |{v1,v2->v1 > v2}  |"gt"  || true
        WPTVersion.get("1.18")  | WPTVersion.get("1.19")    |{v1,v2->v1 > v2}  |"gt"  || false

        WPTVersion.get("1.19")  | WPTVersion.get("1.19")    |{v1,v2->v1 >= v2} |"gte" || true
        WPTVersion.get("1.19")  | WPTVersion.get("1.18")    |{v1,v2->v1 >= v2} |"gte" || true
        WPTVersion.get("1.18")  | WPTVersion.get("1.19")    |{v1,v2->v1 >= v2} |"gte" || false

        WPTVersion.get("1.19")  | WPTVersion.get("1.19")    |{v1,v2->v1 < v2}  |"lt"  || false
        WPTVersion.get("1.19")  | WPTVersion.get("1.18")    |{v1,v2->v1 < v2}  |"lt"  || false
        WPTVersion.get("1.18")  | WPTVersion.get("1.19")    |{v1,v2->v1 < v2}  |"lt"  || true

        WPTVersion.get("1.19")  | WPTVersion.get("1.19")    |{v1,v2->v1 <= v2} |"lte" || true
        WPTVersion.get("1.19")  | WPTVersion.get("1.18")    |{v1,v2->v1 <= v2} |"lte" || false
        WPTVersion.get("1.18")  | WPTVersion.get("1.19")    |{v1,v2->v1 <= v2} |"lte" || true
    }

    def "test version caching"(){
        given:
        int sizeBefore = WPTVersion.cachedVersions.size()
        when: "We get repeatedly to versions, which weren't in the cache before"
        WPTVersion.get("300.2")
        WPTVersion.get("300.2")
        WPTVersion.get("300.2")

        WPTVersion.get("300.1")
        WPTVersion.get("300.1")

        then: "The cache size should only increase by 2"
        WPTVersion.cachedVersions.size() == sizeBefore + 2
    }
}
