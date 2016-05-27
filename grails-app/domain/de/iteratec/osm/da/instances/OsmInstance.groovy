package de.iteratec.osm.da.instances

/**
 * Representation of an OpenSpeedMonitor-Instance which uses this Service.
 */
class OsmInstance {

    String name
    /**
     * URL to the REST-API to get further Informations
     */
    String url

    /**
     *
     */
    Map<String, OsmMapping> osmMappings
    static embedded = ['osmMappings']

    static constraints = {
    }
}
