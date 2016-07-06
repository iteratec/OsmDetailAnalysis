package de.iteratec.osm.da.har

import de.iteratec.osm.da.asset.Connectivity

/**
 * Stores all informations to fetch data from a WPT-Instance and to convert it into the given Assets
 */
class FetchJob {
    
    Date created = new Date()

    long osmInstance
    long jobGroupId
    String wptBaseURL
    String wptTestId
    String wptVersion
    Connectivity connectivity = new Connectivity()

    static embedded = ['connectivity']


    void setBandWidthUp(int up){
        connectivity.bandwithUp = up
    }

    void setBandWithDown(int down){
        connectivity.bandwidhtDown = down
    }

    void setLatency(int latency){
        connectivity.latency = latency
    }

    void setPacketLoss(int loss){
        connectivity.packetLoss = loss
    }


    static constraints = {
    }
}
