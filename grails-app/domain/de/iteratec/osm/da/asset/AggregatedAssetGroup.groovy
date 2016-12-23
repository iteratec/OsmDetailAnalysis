package de.iteratec.osm.da.asset

import com.mongodb.BasicDBObject

class AggregatedAssetGroup {
    long osmInstance
    long jobGroup
    String mediaType
    long browser
    String subtype
    long epochTimeStarted
    long measuredEvent
    String host
    long page
    int loadTimeMs_avg
    int loadTimeMs_min
    int loadTimeMs_max
    int ttfb_avg
    int ttfb_min
    int ttfb_max
    int downloadTime_avg
    int downloadTime_min
    int downloadTime_max
    int sslTime_avg
    int sslTime_min
    int sslTime_max
    int connectTime_avg
    int connectTime_min
    int connectTime_max
    int dnsTime_avg
    int dnsTime_min
    int dnsTime_max
    int bytesIn_avg
    int bytesIn_min
    int bytesIn_max
    int bytesOut_avg
    int bytesOut_min
    int bytesOut_max
    int count

    static constraints = {
    }
    static mapping = {
        compoundIndex epochTimeStarted:-1, jobGroup:1, page:1, browser:1, location:1, measuredEvent:1
    }
}
