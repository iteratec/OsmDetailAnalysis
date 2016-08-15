package de.iteratec.osm.da.wpt

/**
 * Describes a phase within the loading of a website
 */
enum LoadPhase {

    DomTime("domTime"), //ready event in java
    LoadTime("LoadTime"), //All resources within the dom has been loaded
    FullyLoaded("FullyLoaded") //all requests started from scripts are done

    private String value

    private LoadPhase(String value) {
        this.value = value
    }

    @Override
    String toString() {
        return value
    }
}