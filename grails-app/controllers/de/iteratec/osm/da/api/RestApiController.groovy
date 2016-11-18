package de.iteratec.osm.da.api

import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FetchBatch
import de.iteratec.osm.da.fetch.Priority
import de.iteratec.osm.da.mapping.MappingUpdate
import de.iteratec.osm.da.mapping.OsmDomain
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import de.iteratec.osm.da.mapping.MappingService
import grails.converters.JSON
import grails.validation.Validateable


class RestApiController {

    AssetRequestPersistenceService assetRequestPersistenceService
    MappingService mappingService
    WptDetailResultDownloadService wptDetailResultDownloadService
    public static final String DEFAULT_ACCESS_DENIED_MESSAGE = "Access denied! A valid API-Key with sufficient access rights is required!"

    /**
     * Sends message with given httpStatus and message as http response and breaks action (no subsequent
     * action code is executed).
     * @param httpStatus
     * @param message
     */
    private void sendSimpleResponseAsStream(Integer httpStatus, String message) {
        response.setContentType('text/plain;charset=UTF-8')
        response.status = httpStatus
        render message
    }

    def securedViaApiKeyPersistAssetsForWptResult(PersistenceCommand command){
        if (command.hasErrors()) {
            StringWriter sw = new StringWriter()
            command.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            sendSimpleResponseAsStream(400, sw.toString())
            return
        }
        Long osmInstanceId = mappingService.getOSMInstanceId(command.osmUrl)
        if(!osmInstanceId){
            sendSimpleResponseAsStream(400, "Osm with URL ${command.osmUrl} isn't registered")
            return
        }
        if(!WPTVersion.validWPTVersion(command.wptVersion)){
            sendSimpleResponseAsStream(400,"WPT Version ${command.wptVersion} is not valid")
            return
        }
        wptDetailResultDownloadService.addNewFetchJobToQueue(osmInstanceId,command.jobId, command.jobGroupId,command.wptServerBaseUrl,command.wptTestId, command.wptVersion, Priority.Normal)
        sendSimpleResponseAsStream(200,"Added to normalPriorityQueue")
    }
    def persistAssetsBatchJob(PersistenceBatchCommand command){
        log.debug("Got a new PersitenceBatchCommand with parameters osmUlr=\"${command.osmUrl}\" callbackUrl=\"${command.callbackUrl}\" callbackJobId=\"${command.callbackJobId}\" persistanceJobList=\"${command.persistanceJobList}\"")
        if (command.hasErrors()) {
            StringWriter sw = new StringWriter()
            command.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            log.error("PersistenceBatchCommand has the following errors: ${sw.toString()}")
            sendSimpleResponseAsStream(400, sw.toString())
            return
        }

        Long osmInstanceId = mappingService.getOSMInstanceId(command.osmUrl)
        if(!osmInstanceId){
            sendSimpleResponseAsStream(400, "Osm with URL ${command.osmUrl} isn't registered")
            return
        }
        FetchBatch fetchBatch = new FetchBatch(callbackUrl:command.callbackUrl,osmUrl:command.osmUrl,callBackId:command.callbackJobId)
        int numberOfFetchJobs = 0
        command.persistanceJobList.each{
            numberOfFetchJobs+=wptDetailResultDownloadService.addNewFetchJobToQueue(osmInstanceId,it.jobId, it.jobGroupId,it.wptServerBaseUrl,[it.wptTestId], it.wptVersion,Priority.Normal, fetchBatch)
        }
        fetchBatch.countFetchJobs = numberOfFetchJobs
        fetchBatch.queuingDone = true
        fetchBatch.save(flush:true)
        log.debug("Queued ${numberOfFetchJobs} FetchJobs.")
        sendObjectAsJSON(numberOfFetchJobs,false)
    }

    def getAssetRequestGroup(GetAssetRequestGroupCommand command){
        if (command.hasErrors()) {
            StringWriter sw = new StringWriter()
            command.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            sendSimpleResponseAsStream(400, sw.toString())
            return
        }
        AssetRequestGroup assetRequestGroup = assetRequestPersistenceService.getAssetRequestGroup(command.wptServerBaseUrl, command.wptTestId)
        if(!assetRequestGroup){
            sendSimpleResponseAsStream(400, "No data found")
            return
        }
        sendObjectAsJSON(assetRequestGroup,true)

    }

    /**
     *
     * Example: curl http://localhost:8080/restApi/updateMapping --data "osmUrl=http://openspeedmonitor.org&JobGroup.1=AJob&JobGroup.2=AnotherJob&Location.2=ALocation"
     * @param command
     * @return
     */
    def securedViaApiKeyUpdateMapping(MappingCommand command){
        if (command.hasErrors()) {
            StringWriter sw = new StringWriter()
            command.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            sendSimpleResponseAsStream(400, sw.toString())
            return
        }
        OsmInstance instance = OsmInstance.findByUrl(command.osmUrl)
        if(command.Browser) mappingService.updateMapping(instance,new MappingUpdate(domain: OsmDomain.Browser, update: command.Browser))
        if(command.JobGroup) mappingService.updateMapping(instance, new MappingUpdate(domain: OsmDomain.JobGroup, update: command.JobGroup))
        if(command.Location) mappingService.updateMapping(instance, new MappingUpdate(domain: OsmDomain.Location, update: command.Location))
        if(command.MeasuredEvent) mappingService.updateMapping(instance, new MappingUpdate(domain: OsmDomain.MeasuredEvent, update: command.MeasuredEvent))
        sendSimpleResponseAsStream(200,"Mapping was updated")

    }

    def securedViaApiKeyUpdateOsmUrl(UrlUpdateCommand command){
        if (command.hasErrors()) {
            StringWriter sw = new StringWriter()
            command.errors.getFieldErrors().each { fieldError ->
                sw << "Error field ${fieldError.getField()}: ${fieldError.getCode()}\n"
            }
            sendSimpleResponseAsStream(400, sw.toString())
            return
        }
        OsmInstance instance = OsmInstance.findByUrl(command.osmUrl)
        mappingService.updateOsmUrl(instance, command.newOsmUrl)
        sendSimpleResponseAsStream(200,"Url updated to $command.newOsmUrl")
    }

    /**
     * <p>
     * Sends the object rendered as JSON. All public getters are used to
     * render the result. This call should be placed as last statement, the
     * return statement, of an action.
     * </p>
     *
     * @param objectToSend
     *         The object to render end to be sent to the client,
     *         not <code>null</code>.
     * @param usePrettyPrintingFormat
     *         Set to <code>true</code> if the JSON should be "pretty
     *         formated" (easy to read but larger file).
     * @return Always <code>null</code>.
     * @throws NullPointerException
     *         if {@code objectToSend} is <code>null</code>.
     */
    private void sendObjectAsJSON(Object objectToSend, boolean usePrettyPrintingFormat) {
        JSON converter = new JSON(target: objectToSend)
        converter.setPrettyPrint(usePrettyPrintingFormat)
        render converter
    }
}

public class OsmCommand implements Validateable{
    String apiKey
    String osmUrl

    static constraints = {
        osmUrl(nullable:false)
    }

    void setOsmUrl(String url) {
        this.osmUrl = OsmInstance.ensureUrlHasTrailingSlash(url)
    }
}

public class PersistenceCommand extends OsmCommand{
    String wptVersion
    List<String> wptTestId
    String wptServerBaseUrl
    Long jobGroupId
    Long jobId

    static constraints = {
        apiKey(validator: { String currentKey, PersistenceCommand cmd ->
            List<ApiKey> apiKeys = ApiKey.findAllBySecretKey(currentKey)
            ApiKey validApiKey
            apiKeys.each {
                if (it.osmInstance.url == cmd.osmUrl) validApiKey = it
            }
            if (!validApiKey||!validApiKey.allowedToTriggerFetchJobs) return [RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
        osmUrl(nullable:false)
        wptTestId(nullable:false)
        wptServerBaseUrl(nullable:false)
        jobGroupId(nullable:false)
        jobId(nullable:false)
        wptVersion(nullable:false)
    }



    @Override
    public String toString() {
        return "PersistenceCommand{" +
                "osmUrl='" + osmUrl + '\'' +
                ", wptTestId='" + wptTestId + '\'' +
                ", wptServerBaseUrl='" + wptServerBaseUrl + '\'' +
                ", jobGroupId=" + jobGroupId +
                ", jobId=" + jobId +
                '}';
    }
}
public class PersistenceBatchCommand extends OsmCommand{
    String callbackUrl
    String callbackJobId
    List persistanceJobList

    static constraints = {
        apiKey(validator: { String currentKey, PersistenceBatchCommand cmd ->
            List<ApiKey> apiKeys = ApiKey.findAllBySecretKey(currentKey)
            ApiKey validApiKey
            apiKeys.each {
                if (it.osmInstance.url == cmd.osmUrl) validApiKey = it
            }
            if (!validApiKey||!validApiKey.allowedToTriggerFetchJobs) return [RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
        osmUrl(nullable:false)
        callbackUrl nullable: false
    }

}

public class UrlUpdateCommand extends OsmCommand{
    String newOsmUrl
    static constraints = {
        apiKey(validator: { String currentKey, UrlUpdateCommand cmd ->
            List<ApiKey> apiKeys = ApiKey.findAllBySecretKey(currentKey)
            ApiKey validApiKey
            apiKeys.each {
                if (it.osmInstance.url == cmd.osmUrl) validApiKey = it
            }
            if (!validApiKey||!validApiKey.allowedToUpdateOsmUrl) return [RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
    }
}
public class GetAssetRequestGroupCommand {
    String wptTestId
    String wptServerBaseUrl

}

public class MappingCommand extends OsmCommand{
    Map<Long, String> JobGroup
    Map<Long, String> Location
    Map<Long, String> Browser
    Map<Long, String> MeasuredEvent
    static constraints = {
        apiKey(validator: { String currentKey, MappingCommand cmd ->
            List<ApiKey> apiKeys = ApiKey.findAllBySecretKey(currentKey)
            ApiKey validApiKey
            apiKeys.each {
                if (it.osmInstance.url == cmd.osmUrl) validApiKey = it
            }
            if (!validApiKey||!validApiKey.allowedToUpdateMapping) return [RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
    }
    @Override
    public String toString() {
        return "MappingCommand{" +
                "osmUrl='" + osmUrl + '\'' +
                ", JobGroup=" + JobGroup +
                ", Location=" + Location +
                ", Browser=" + Browser +
                ", MeasuredEvent=" + MeasuredEvent +
                '}';
    }
}
