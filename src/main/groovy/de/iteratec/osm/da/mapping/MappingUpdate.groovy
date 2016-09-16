package de.iteratec.osm.da.mapping

/**
 * Created by benni on 16.09.16.
 */
class MappingUpdate{


    OsmDomain domain
    Map<Long,String> update

    public static List<MappingUpdate> createMappingList(def json){
        json."target".each{String domain, Map<Long,String> value->
            createMapping(domain, value)
        }
    }

    public static MappingUpdate createMapping(String domain, Map<Long,String> value){
        return new MappingUpdate(domain: OsmDomain.valueOf(domain), update: value)
    }

    public def each(Closure c){
        update.each {
            c.call(it.key,it.value)
        }
    }
}
