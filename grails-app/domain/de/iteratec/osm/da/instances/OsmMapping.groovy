package de.iteratec.osm.da.instances

import de.iteratec.osm.da.mapping.OsmDomain

/**
 * Maps ids from a domain to name. Like JobGroup with ID 1 = 'WK'
 */
class OsmMapping {

    OsmDomain domain
    Map<Long, String> mapping = [:]
    static embedded = ['mapping']

    static constraints = {
    }

}
