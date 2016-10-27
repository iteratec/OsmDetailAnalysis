package de.iteratec.osm.da.mapping

import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.instances.OsmMapping
import de.iteratec.osm.da.wpt.resolve.exceptions.OsmMappingDoesntExistException
import de.iteratec.osm.da.wpt.resolve.exceptions.OsmNotAvailableException
import grails.transaction.Transactional

class MappingService {

    HttpRequestService httpRequestService

    /**
     * Updates the mappings of a given domain from an OsmInstance. This doesn't affect
     * mappings which are not in the update map.
     * @param instance
     * @param domain
     * @param toUpdate Mappings which should be updated
     */
    void updateMapping(OsmInstance instance, MappingUpdate mappingUpdate){
        mappingUpdate?.each {key,value ->
            instance.getMapping(mappingUpdate.domain).mapping."$key" = value
        }
        instance.save(flush:true, failOnError:true)
    }


    /**
     * Checks if every domain has the needed id mappings. If a mapping is missing, we will contact the OsmInstance to give us the needed values
     * @return True if this map contains all values. If there is still a missing value, even after the upate try, this method will return false
     */
    synchronized boolean updateIfIdMappingsDoesntExist(long instanceId, Map<OsmDomain, List<Long>> domains){
        OsmInstance instance = OsmInstance.get(instanceId)
        Map<String, List<Long>> domainsToUpdate = [:]
        domains.each { OsmDomain domain, List<Long> idsNeeded->
            List<Long> missingIds = idsNeeded - instance.getMapping(domain).mapping.keySet()*.toLong()
            if(missingIds.size()>0) domainsToUpdate."$domain" = missingIds
        }
        boolean allUpdatesDone = true
        if(domainsToUpdate.size() > 0 ){
            log.debug("OsmInstance $instanceId needs an mapping update")
            try {
                List<MappingUpdate> updates = getIdUpdate(domainsToUpdate, instance)
                applyUpdates(updates,instance,domainsToUpdate)
            } catch (ConnectException e){
                log.error("Could't connect to osm instance $instanceId to get a mapping update. \n $e")
                throw new OsmNotAvailableException(instance.url)
            }
        }
        return allUpdatesDone
    }

    /**
     * Checks if every domain has the needed id mappings. If a mapping is missing, we will contact the OsmInstance to give us the needed values
     * @return True if this map contains all values. If there is still a missing value, even after the upate try, this method will return false
     */
    synchronized boolean updateIfNameMappingsDoesntExist(long instanceId, Map<OsmDomain, List<String>> domains){
        OsmInstance instance = OsmInstance.get(instanceId)
        Map<String, List<Long>> domainsToUpdate = [:]
        domains.each { OsmDomain domain, List<String> namesNeeded->
            List<String> missingNames = namesNeeded - instance.getMapping(domain).mapping.values()
            if(missingNames.size()>0) domainsToUpdate."$domain" = missingNames
        }
        boolean allUpdatesDone = true
        if(domainsToUpdate.size() > 0 ){
            log.debug("OsmInstance $instanceId needs an mapping update")
            try {
                List<MappingUpdate> updates = getNameUpdate(domainsToUpdate, instance)
                applyUpdates(updates,instance,domainsToUpdate)
            }catch (OsmMappingDoesntExistException e){
                //We have to pass this trough. Every other exception should be cached in the next block
                throw e
            }catch (Exception e){
                log.error("Could't connect to osm instance $instanceId to get a mapping update. \n $e")
                throw new OsmNotAvailableException(instance.url)
            }
        }
        return allUpdatesDone
    }

    /**
     * Applies all updates to an OsmInstance. If there where updates missing this method will throw an OsmMappingDoesntExistException.
     * @param updates
     * @param instance
     * @param domainsToUpdate
     */
    void applyUpdates(List<MappingUpdate> updates, OsmInstance instance, Map<String, List<Long>> domainsToUpdate  ){
        updates.each {
            updateMapping(instance, it)
        }
        //check if something is missing
        Map<OsmDomain, List<Long>> missingUpdates = [:]
        boolean allUpdatesDone = true
        updates.each {MappingUpdate update ->
            boolean updatesDone = domainsToUpdate[update.domain.toString()].size() == update.updateCount()
            allUpdatesDone &= updatesDone
            if(!updatesDone){
                missingUpdates.put(update.domain, domainsToUpdate[update.domain.toString()] - update.update.keySet())
            }
        }
        if(!missingUpdates.isEmpty()) throw new OsmMappingDoesntExistException(missingUpdates)
    }



    /**
     * Sends a Request to osm instance to get missing mappings.
     * @param idsToUpdate Map of Strings, which maps to a list of ids, which are missing within the domain
     * @param instance The osm instance which should be updated
     * @return A map with the answer from the osm instance
     */
    private List<MappingUpdate> getIdUpdate(Map<String, List<Long>> idsToUpdate, OsmInstance instance){
        def updateJson = httpRequestService.getJsonResponseFromOsm(instance.url,"/rest/domain/namesForIds/", idsToUpdate)
        return MappingUpdate.createMappingList(updateJson)
    }

    /**
     * Sends a Request to osm instance to get missing mappings.
     * @param namesToUpdate Map of Strings, which maps to a list of names, which are missing within the domain
     * @param instance The osm instance which should be updated
     * @return A map with the answer from the osm instance
     */
    private def getNameUpdate(Map<String, List<String>> namesToUpdate, OsmInstance instance){
        def jsonResult = httpRequestService.getJsonResponseFromOsm(instance.url,"/rest/domain/idsForNames/", namesToUpdate)
        return MappingUpdate.createMappingList(jsonResult)
    }
    Long getOSMInstanceId(String url){
        return OsmInstance.findByUrl(url)?.id
    }

    Map<Long, String> getOsmMapping(OsmDomain domain, OsmInstance osmInstance) {
        return osmInstance.getMapping(domain).mapping
    }

    /**
     * Returns the name of a id within a domain of a wpt instance
     * @param osmId The id of the osm instance
     * @param domain The domain to search
     * @param id The id to search
     * @return
     */
    String getMappingEntryFromOsm(Long osmId, OsmDomain domain, long id){
        return getOsmMapping(domain, OsmInstance.get(osmId))."$id"
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

    Map<Long, String> getBrowserMappings(OsmInstance osmInstance) {
        getOsmMapping(OsmDomain.Browser, osmInstance)
    }

    Map<Long, String> getJobMappings(OsmInstance osmInstance) {
        getOsmMapping(OsmDomain.Job, osmInstance)
    }

    Map<Long, String> getPageMappings(OsmInstance osmInstance) {
        getOsmMapping(OsmDomain.Page, osmInstance)
    }

    Map<Long, String> getMeasuredEventMappings(OsmInstance osmInstance) {
        getOsmMapping(OsmDomain.MeasuredEvent, osmInstance)
    }

    Map<Long, String> getJobGroupMappings(OsmInstance osmInstance) {
        getOsmMapping(OsmDomain.JobGroup, osmInstance)
    }
}
