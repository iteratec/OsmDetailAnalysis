package de.iteratec.osm.da.asset

class Connectivity {

    int bandwidthDown
    int bandwidthUp
    int latency
    int packetLoss

    static constraints = {
        bandwidthDown(min:0)
        bandwidthUp(min:0)
        latency(min:0)
        packetLoss(min:0)
    }
}
