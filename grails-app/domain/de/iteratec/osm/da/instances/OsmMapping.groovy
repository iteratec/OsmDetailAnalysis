package de.iteratec.osm.da.instances

/**
 * Maps ids from a domain to name. Like JobGroup with ID 1 = 'WK'
 */
class OsmMapping {

    String domain
    Map<Long, String> mapping
    static embedded = ['mapping']

    static constraints = {
    }

}
