package de.iteratec.oms.da.external.mapping

enum OsmDomain {

    Browser("Browser"), JobGroup("JobGroup"), Location("Location"), MeasuredEvent("MeasuredEvent"), Page("Page")

    private String value

    private OsmDomain(String value) {
        this.value = value
    }

    @Override
    String toString() {
        return value
    }
}
