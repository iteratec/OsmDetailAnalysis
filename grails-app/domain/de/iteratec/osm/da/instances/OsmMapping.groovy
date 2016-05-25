package de.iteratec.osm.da.instances

class OsmMapping {

    String domain
    Map<Long, String> mapping
    static embedded = ['mapping']

    static constraints = {
    }

}
