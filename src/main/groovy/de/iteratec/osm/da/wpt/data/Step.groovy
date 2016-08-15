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
