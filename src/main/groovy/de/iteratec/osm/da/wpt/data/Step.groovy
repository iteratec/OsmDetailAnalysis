package de.iteratec.osm.da.wpt.data

/**
 * On step during a wpt test
 */
class Step {

    List<Request>  requests
    int run
    int stepNumber
    int docTime
    String url
    String eventName
    long epochTimeStarted
    boolean isMedian
    boolean isFirstView


    @Override
    public String toString() {
        return "Step{" +
                "requests=" + requests.size() +
                ", run=" + run +
                ", stepNumber=" + stepNumber +
                ", docTime=" + docTime +
                ", url='" + url + '\'' +
                ", eventName='" + eventName + '\'' +
                ", epochTimeStarted='" + epochTimeStarted + '\'' +
                ", isMedian=" + isMedian +
                ", isFirstView=" + isFirstView +
                '}';
    }
}
