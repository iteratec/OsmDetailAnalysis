package de.iteratec.osm.da.mapping

enum OsmDomain {

    Browser("Browser"), JobGroup("JobGroup"), Location("Location"), MeasuredEvent("MeasuredEvent"), Page("Page"), Job("Job")

    private String value

    private OsmDomain(String value) {
        this.value = value
    }

    @Override
    String toString() {
        return value
    }
}
