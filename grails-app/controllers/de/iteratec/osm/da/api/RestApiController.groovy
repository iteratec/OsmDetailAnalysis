package de.iteratec.osm.da.api

import de.iteratec.osm.da.mapping.OsmDomain
import de.iteratec.osm.da.wpt.data.WPTVersion
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.wpt.WptDetailResultDownloadService
import de.iteratec.osm.da.mapping.MappingService


class RestApiController {

    MappingService mappingService
    WptDetailResultDownloadService wptDetailResultDownloadService


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

    def persistAssetsForWptResult(PersistenceCommand command){
        Long osmInstanceId = mappingService.getOSMInstanceId(command.osmUrl)
        if(!osmInstanceId){
            sendSimpleResponseAsStream(400, "Osm with URL ${command.osmUrl} isn't registered")
            return
        }
        if(!WPTVersion.validWPTVersion(command.wptVersion)){
            sendSimpleResponseAsStream(400,"WPT Version ${command.wptVersion} is not valid")
            return
        }
        wptDetailResultDownloadService.addToQueue(osmInstanceId,command.jobId, command.jobGroupId,command.wptServerBaseUrl,command.wptTestId, command.wptVersion)
        sendSimpleResponseAsStream(200,"Added to queue")
    }

    /**
     *
     * Example: curl http://localhost:8080/restApi/updateMapping --data "osmUrl=http://openspeedmonitor.org&JobGroup.1=AJob&JobGroup.2=AnotherJob&Location.2=ALocation"
     * @param command
     * @return
     */
    def updateMapping(MappingCommand command){
        OsmInstance instance = OsmInstance.findByUrl(command.osmUrl)
        if(command.Browser) mappingService.updateMapping(instance,OsmDomain.Browser, command.Browser)
        if(command.JobGroup) mappingService.updateMapping(instance,OsmDomain.JobGroup, command.JobGroup)
        if(command.Location) mappingService.updateMapping(instance,OsmDomain.Location, command.Location)
        if(command.MeasuredEvent) mappingService.updateMapping(instance,OsmDomain.MeasuredEvent, command.MeasuredEvent)
        sendSimpleResponseAsStream(200,"Mapping was updated")

    }

    def updateOsmUrl(UrlUpdateCommand command){
        OsmInstance instance = OsmInstance.findByUrl(command.osmUrl)
        mappingService.updateOsmUrl(instance, command.newOsmUrl)
        sendSimpleResponseAsStream(200,"Url updated to $command.newOsmUrl")
    }


}

public class PersistenceCommand{

    String osmUrl
    String wptVersion
    List<String> wptTestId
    String wptServerBaseUrl
    Long jobGroupId
    Long jobId

    static constraints = {
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

public class UrlUpdateCommand{
    String osmUrl
    String newOsmUrl
}

public class MappingCommand{
    String osmUrl
    Map<Long, String> JobGroup
    Map<Long, String> Location
    Map<Long, String> Browser
    Map<Long, String> MeasuredEvent

    static constraints = {
        osmUrl(nullable:false)
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
