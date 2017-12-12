package de.iteratec.osm.da.asset

class AggregatedAsset {
    String host
    String url
    String subtype
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
}
