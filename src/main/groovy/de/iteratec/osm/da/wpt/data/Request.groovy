package de.iteratec.osm.da.wpt.data

import de.iteratec.osm.da.wpt.LoadPhase

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
    int loadEnd
    int bytesIn
    int bytesOut
    int sslNegotiationTimeMs
    String contentType
    int indexWithinStep
    int dnsTimeMs
    //Current phase while the asset request was started
    LoadPhase startPhase
    //Current phase while the asset was done loading
    LoadPhase endPhase


    void setPhases(int domTime, int loadTime){
       setStartPhase(domTime, loadTime)
       setEndPhase(domTime, loadTime)
    }

    void setStartPhase(int domTime, int loadTime){
        if(loadStart > loadTime){
            startPhase = LoadPhase.FullyLoaded
        }else if(loadStart > domTime){
            startPhase = LoadPhase.LoadTime
        } else{
            startPhase = LoadPhase.DomTime
        }

    }
    void setEndPhase(int domTime, int loadTime){
        if(loadEnd > loadTime){
            endPhase = LoadPhase.FullyLoaded
        }else if(loadEnd > domTime){
            endPhase = LoadPhase.LoadTime
        } else{
            endPhase = LoadPhase.DomTime
        }
    }

}
