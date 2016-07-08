package de.iteratec.osm.da.external.instances

/**
 * Representation of an OpenSpeedMonitor-Instance which uses this Service.
 *
 * Example:
 *
 * {
 *     name:OsmDemo
 *     url:demo.openspeedmonitor.org
 *     osmMappings:{
 *         "Job":{
 *             domain:"Job",
 *             mappings:{
 *                 1: "TestJob"
 *             }
 *         }
 *     }
 * }
 *
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
    Map<String, OsmMapping> osmMappings = [:]
    static embedded = ['osmMappings']

    static constraints = {
    }
}
