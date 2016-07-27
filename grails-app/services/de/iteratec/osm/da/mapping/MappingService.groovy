package de.iteratec.osm.da.mapping

import de.iteratec.osm.da.HTTPRequestService
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.instances.OsmMapping
import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class MappingService {

    HTTPRequestService httpRequestService

    /**
     * Updates the mappings of a given domain from an OsmInstance. This doesn't affect
     * mappings which are not in the update map.
     * @param instance
     * @param domain
     * @param toUpdate Mappings which should be updated
     */
    void updateMapping(OsmInstance instance, OsmDomain domain, Map<Long, String> toUpdate){
        toUpdate?.each {key,value ->
            instance.getMapping(domain).mapping."$key" = value
        }
        instance.save(flush:true, failOnError:true)
    }


    /**
     * Checks if every domain has the needed id mappings. If a mapping is missing, we will contact the OsmInstance to give us the needed values
     * @return True if this map contains all values. If there is still a missing value, even after the upate try, this method will return false
     */
    boolean updateIfIdMappingsDoesntExist(OsmInstance instance, Map<OsmDomain, List<Long>> domains){
        Map<String, List<Long>> domainsToUpdate = [:]
        domains.each { OsmDomain domain, List<Long> idsNeeded->
            List<Long> missingIds = idsNeeded - instance.getMapping(domain).mapping.keySet()*.toLong()
            if(missingIds.size()>0) domainsToUpdate."$domain" = missingIds
        }
        if(domainsToUpdate.size() > 0 ){
            def updates = getIdUpdate(domainsToUpdate, instance)
            updates."target".each{String domain, Map<Long,String> value->
                updateMapping(instance, OsmDomain.valueOf(domain), value)
            }
            //TODO check if something is missing
        }
        return true
    }

    /**
     * Checks if every domain has the needed id mappings. If a mapping is missing, we will contact the OsmInstance to give us the needed values
     * @return True if this map contains all values. If there is still a missing value, even after the upate try, this method will return false
     */
    boolean updateIfNameMappingsDoesntExist(OsmInstance instance, Map<OsmDomain, List<String>> domains){
        Map<String, List<Long>> domainsToUpdate = [:]
        domains.each { OsmDomain domain, List<String> namesNeeded->
            List<String> missingNames = namesNeeded - instance.getMapping(domain).mapping.values()
            if(missingNames.size()>0) domainsToUpdate."$domain" = missingNames
        }
        if(domainsToUpdate.size() > 0 ){
            def updates = getNameUpdate(domainsToUpdate, instance)
            updates."target".each{String domain, Map<Long,String> value->
                updateMapping(instance, OsmDomain.valueOf(domain), value)
            }
            //TODO check if something is missing
        }
        return true
    }

    /**
     * Sends a Request to osm instance to get missing mappings.
     * @param idsToUpdate Map of Strings, which maps to a list of ids, which are missing within the domain
     * @param instance The osm instance which should be updated
     * @return A map with the answer from the osm instance
     */
    private def getIdUpdate(Map<String, List<Long>> idsToUpdate, OsmInstance instance){
        return httpRequestService.getJsonResponse(instance.url,"rest/domain/namesForIds", idsToUpdate)
    }

    /**
     * Sends a Request to osm instance to get missing mappings.
     * @param namesToUpdate Map of Strings, which maps to a list of names, which are missing within the domain
     * @param instance The osm instance which should be updated
     * @return A map with the answer from the osm instance
     */
    private def getNameUpdate(Map<String, List<String>> namesToUpdate, OsmInstance instance){
        return httpRequestService.getJsonResponse(instance.url,"/rest/domain/idsForNames/", namesToUpdate)
    }
    Long getOSMInstanceId(String url){
        return OsmInstance.findByUrl(url)?.id
    }

    /**
     * Returns the name of a id within a domain of a wpt instance
     * @param osmId The id of the osm instance
     * @param domain The domain to search
     * @param id The id to search
     * @return
     */
    String getMappingEntryFromOsm(Long osmId, OsmDomain domain, long id){
        OsmInstance osm = OsmInstance.findById(osmId)
        return osm.getMapping(domain).mapping."$id"
    }

    String getNameForBrowserId(long osmId, long id){
        return getMappingEntryFromOsm(osmId,OsmDomain.Browser,id)
    }
    String getNameForLocationId(long osmId, long id){
        return getMappingEntryFromOsm(osmId, OsmDomain.Location,id)
    }
    String getNameForJobGroupId(long osmId, long id){
        return getMappingEntryFromOsm(osmId, OsmDomain.JobGroup,id)
    }
    String getNameForMeasuredEventId(long osmId, long id){
        return getMappingEntryFromOsm(osmId, OsmDomain.MeasuredEvent,id)
    }
    String getNameForPageId(long osmId, long id){
        return getMappingEntryFromOsm(osmId, OsmDomain.Page,id)
    }

    /**
     * Returns the id of a given name in the domain of osm instance
     * @param osmId The id of the osm instance
     * @param domain The domain to search
     * @param name  The name of a domain entry to search
     * @return
     */
    long getMappingEntryFromOsm(Long osmId, OsmDomain domain, String name){
        OsmInstance osm = OsmInstance.findById(osmId)
        OsmMapping mapping = osm.getMapping(domain)
        int id = -1
        mapping.mapping.find {k,v->
            if(v == name){
                id = k.toLong()
                return true
            }
            return false
        }
        return id
    }

    long getIdForJobGroupName(long osmId, String name){
        return getMappingEntryFromOsm(osmId, OsmDomain.JobGroup,name)
    }
    long getIdForBrowserName(long osmId, String name){
        return getMappingEntryFromOsm(osmId,OsmDomain.Browser,name)
    }
    long getIdForLocationName(long osmId, String name){
        return getMappingEntryFromOsm(osmId, OsmDomain.Location,name)
    }
    long getIdForMeasuredEventName(long osmId, String name){
        return getMappingEntryFromOsm(osmId, OsmDomain.MeasuredEvent,name)
    }
    long getIdForPageName(long osmId, String name){
        return getMappingEntryFromOsm(osmId, OsmDomain.Page,name)
    }

    def updateOsmUrl(OsmInstance osmInstance, String newUrl) {
        osmInstance.setUrl(newUrl).save(flush:true)
    }
}
