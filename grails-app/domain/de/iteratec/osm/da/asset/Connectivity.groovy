package de.iteratec.osm.da.asset

class Connectivity {

    int bandwidhtDown
    int bandwithUp
    int latency
    int packetLoss

    static constraints = {
        bandwidhtDown(min:0)
        bandwithUp(min:0)
        latency(min:0)
        packetLoss(min:0)
    }
}
