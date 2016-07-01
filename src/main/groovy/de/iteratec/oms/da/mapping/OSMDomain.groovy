package de.iteratec.oms.da.mapping

enum OSMDomain {

    Job("Job"), Browser("Browser"), JobGroup("JobGroup"), Location("Location")

    private String value

    private OSMDomain(String value) {
        this.value = value
    }

    @Override
    String toString() {
        return value
    }
}
