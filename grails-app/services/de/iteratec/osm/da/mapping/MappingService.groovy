package de.iteratec.osm.da.mapping

import de.iteratec.osm.da.instances.OsmInstance
import grails.transaction.Transactional

@Transactional
class MappingService {

    /**
     * Updates the mappings of a given domain from an OsmInstance. This doesn't affect
     * mappings which are not in the update map.
     * @param instance
     * @param domain
     * @param toUpdate Mappings which should be updated
     */
    void updateMapping(OsmInstance instance, String domain, Map<Long, String> toUpdate){
        Map domainMap = instance.osmMappings."$domain"
        if(domain == null){
            domainMap = [:]
            instance.osmMappings."$domain" = domainMap
        }

        toUpdate.each {key,value ->
            domain."$key" = value
        }
        instance.save()
    }

    /**
     * Overrides all mappings of a given domain in an OsmInstance
     * @param instance
     * @param domain
     * @param toUpdate
     */
    void setMapping(OsmInstance instance, String domain, Map<Long, String> toUpdate){
        instance.osmMappings."$domain" = toUpdate
        instance.save()
    }

    /**
     * Checks if every domain has the needed id mapping. If a mapping is missing, we will contact the OsmInstance to give us the needed value
     * @return True if this map contains all values. If there is still a missing value, even after the upate try, this method will return false
     */
    boolean updateIfMappingsDoenstExist(OsmInstance instance, Map<String, List<Long>> domains){
        Map<String, List<Long>> domainsToUpdate = [:].withDefault {[]}
        domains.each { String domain, List<Long> idsNeeded->
            List<Long> missingIds = idsNeeded - instance.osmMappings."$domain".mapping.keySet()
            if(missingIds.size()>0) domainsToUpdate."$domain" =missingIds
        }
        //TODO a request service should now get the missing ids map and request the osm instance to get the missing values
        def updates = [:]
        updates."resolved".each{key, value->
            updateMapping(instance, key, value)
        }
        return !(updates."missing"?.size>0)
    }


}
