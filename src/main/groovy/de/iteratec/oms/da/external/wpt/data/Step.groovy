package de.iteratec.oms.da.external.wpt.data

/**
 * On step during a wpt test
 */
class Step {

    List<Request>  requests
    int run
    int step
    int docTime
    String url
    String eventName
    boolean isMedian
    boolean isFirstView
    boolean isSecondView


    @Override
    public String toString() {
        return "Step{" +
                "requests=" + requests.size() +
                ", run=" + run +
                ", step=" + step +
                ", docTime=" + docTime +
                ", url='" + url + '\'' +
                ", eventName='" + eventName + '\'' +
                ", isMedian=" + isMedian +
                ", isFirstView=" + isFirstView +
                ", isSecondView=" + isSecondView +
                '}';
    }
}
