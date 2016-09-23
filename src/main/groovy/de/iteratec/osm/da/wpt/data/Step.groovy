package de.iteratec.osm.da.wpt.data

/**
 * On step during a wpt test
 */
class Step {

    List<Request>  requests
    int run
    int stepNumber
    String url
    String eventName
    long epochTimeStarted
    boolean isMedian
    boolean isFirstView
    int domTime
    int loadTime
    int fullyLoaded

    public boolean hasMetaValues(){
        return run > -1 &&
                stepNumber > -1 &&
                url &&
                eventName &&
                epochTimeStarted > -1 &&
                domTime > -1 &&
                loadTime > -1 &&
                fullyLoaded > -1
    }

    public boolean hasRequests(){
        return requests?requests.size()>0:false
    }


    @Override
    public String toString() {
        return "Step{" +
                "requests=" + requests.size() +
                ", run=" + run +
                ", stepNumber=" + stepNumber +
                ", url='" + url + '\'' +
                ", eventName='" + eventName + '\'' +
                ", epochTimeStarted='" + epochTimeStarted + '\'' +
                ", isMedian=" + isMedian +
                ", isFirstView=" + isFirstView +
                ", domTime=" + domTime +
                ", loadTime=" + loadTime +
                ", fullyLoaded=" + fullyLoaded +
                '}';
    }
}
