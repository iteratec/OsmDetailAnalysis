package de.iteratec.osm.da.wpt.data

/**
 * Representation of one requests during a step in wpt
 */
class Request {

    String host
    String url
    int loadMs
    int connectTimeMs
    int downloadMs
    int ttfbMs
    int loadStart
    int bytesIn
    int bytesOut
    int sslNegotiationTimeMs
    String contentType
    int indexWithinStep

}
