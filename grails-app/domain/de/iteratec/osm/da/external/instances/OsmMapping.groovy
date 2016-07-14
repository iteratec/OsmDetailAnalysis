package de.iteratec.osm.da.external.instances

import de.iteratec.oms.da.external.mapping.OsmDomain

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
