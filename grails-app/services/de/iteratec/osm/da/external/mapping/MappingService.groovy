package de.iteratec.osm.da.external.mapping

import de.iteratec.oms.da.external.mapping.OSMDomain

import de.iteratec.osm.da.external.instances.OsmInstance
import de.iteratec.osm.da.external.instances.OsmMapping
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
    void updateMapping(OsmInstance instance, OSMDomain domain, Map<Long, String> toUpdate){
        toUpdate.each {key,value ->
            instance.getMapping(domain).mapping."$key" = value
        }
        instance.save(flush:true, failOnError:true)
    }


    /**
     * Checks if every domain has the needed id mapping. If a mapping is missing, we will contact the OsmInstance to give us the needed value
     * @return True if this map contains all values. If there is still a missing value, even after the upate try, this method will return false
     */
    boolean updateIfMappingsDoesntExist(OsmInstance instance, Map<OSMDomain, List<Long>> domains){
        Map<String, List<Long>> domainsToUpdate = [:]
        domains.each { OSMDomain domain, List<Long> idsNeeded->
            List<Long> missingIds = idsNeeded - instance.getMapping(domain).mapping.keySet()
            if(missingIds.size()>0) domainsToUpdate."$domain" = missingIds
        }
        //TODO a request service should now get the missing ids map and request the osm instance to get the missing values
        def updates = [:]
        updates."resolved".each{String domain, Map<Long,String> value->
            updateMapping(instance, OSMDomain.valueOf(domain), value)
        }
        return !(updates."missing"?.size>0)
    }

    Long getOSMInstanceId(String url){
        return OsmInstance.findByUrl(url)?.id
    }

    String getMappingEntryFromOsm(Long osmId, OSMDomain domain, long id){
        OsmInstance osm = OsmInstance.findById(osmId)
        return osm.getMapping(domain)."$id"
    }

    String getNameForBrowserId(long osmId, long id){
        return getMappingEntryFromOsm(osmId,OSMDomain.Browser,id)
    }
    String getNameForLocationId(long osmId, long id){
        return getMappingEntryFromOsm(osmId, OSMDomain.Location,id)
    }
    String getNameForJobGroupId(long osmId, long id){
        return getMappingEntryFromOsm(osmId, OSMDomain.JobGroup,id)
    }
    String getNameForMeasuredEventId(long osmId, long id){
        return getMappingEntryFromOsm(osmId, OSMDomain.MeasuredEvent,id)
    }


    int getMappingEntryFromOsm(Long osmId, OSMDomain domain, String name){
        OsmInstance osm = OsmInstance.findById(osmId)
        OsmMapping mapping = osm.getMapping(domain)
        int id = -1
        mapping.mapping.find {k,v->
            if(v == name){
                id = k
                return true
            }
            return false
        }
        return id
    }

    int getIdForJobGroupName(long osmId, String name){
        return getMappingEntryFromOsm(osmId, OSMDomain.JobGroup,name)
    }
    int getIdForBrowserName(long osmId, String name){
        return getMappingEntryFromOsm(osmId,OSMDomain.Browser,name)
    }
    int getIdForLocationName(long osmId, String name){
        return getMappingEntryFromOsm(osmId, OSMDomain.Location,name)
    }
    int getIdForMeasuredEventName(long osmId, String name){
        return getMappingEntryFromOsm(osmId, OSMDomain.MeasuredEvent,name)
    }


}
