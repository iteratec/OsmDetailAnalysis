package de.iteratec.oms.da.external.mapping

enum OSMDomain {

    Browser("Browser"), JobGroup("JobGroup"), Location("Location"), MeasuredEvent("MeasuredEvent")

    private String value

    private OSMDomain(String value) {
        this.value = value
    }

    @Override
    String toString() {
        return value
    }
}
