package de.iteratec.osm.da.instances

class OsmInstance {

    String name
    String url

    /**
     *
     */
    Map<String, OsmMapping> osmMappings
    static embedded = ['osmMappings']

    static constraints = {
    }
}
