package de.iteratec.osm.da.api

import de.iteratec.oms.da.external.mapping.OsmDomain
import de.iteratec.osm.da.external.wpt.data.WPTVersion
import de.iteratec.osm.da.external.instances.OsmInstance
import de.iteratec.osm.da.external.wpt.WptDetailResultDownloadService
import de.iteratec.osm.da.external.mapping.MappingService


class RestApiController {

    MappingService mappingService
    WptDetailResultDownloadService wptDownloadService


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
        }
        if(!WPTVersion.validWPTVersion(command.wptVersion)){
            sendSimpleResponseAsStream(400,"WPT Version ${command.wptVersion} is not valid")
        }
        wptDownloadService.addToQeue(osmInstanceId,command.jobGroupId,command.wptServerBaseUrl,command.wptTestId, command.wptVersion)
        sendSimpleResponseAsStream(200,"Added to queue")
    }

    /**
     *
     * Example: curl http://localhost:8080/restApi/updateMapping --data "osmUrl=http://openspeedmonitor.org&JobGroup.1=AJob&JobGroup.2=AnotherJob&Location.2=ALocation"
     * @param command
     * @return
     */
    def updateMapping(MappingCommand command){

        OsmInstance url = OsmInstance.findByUrl(command.osmUrl)
        if(command.Browser) mappingService.updateMapping(url,OsmDomain.Browser, command.Browser)
        if(command.JobGroup) mappingService.updateMapping(url,OsmDomain.JobGroup, command.JobGroup)
        if(command.Location) mappingService.updateMapping(url,OsmDomain.Location, command.Location)
        if(command.MeasuredEvent) mappingService.updateMapping(url,OsmDomain.MeasuredEvent, command.MeasuredEvent)
        sendSimpleResponseAsStream(200,"Aye")

    }

    def updateOsmUrl(UrlUpdateCommand command){

    }


}

public class PersistenceCommand{

    String osmUrl
    String wptVersion
    List<String> wptTestId
    String wptServerBaseUrl
    Long jobGroupId

    static constraints = {
        osmUrl(nullable:false)
        wptTestId(nullable:false)
        wptServerBaseUrl(nullable:false)
        jobGroupId(nullable:false)
        wptVersion(nullable:false)
    }


    @Override
    public String toString() {
        return "PersistenceCommand{" +
                "osmUrl='" + osmUrl + '\'' +
                ", wptTestId='" + wptTestId + '\'' +
                ", wptServerBaseUrl='" + wptServerBaseUrl + '\'' +
                ", jobGroupId=" + jobGroupId +
                '}';
    }
}

public class UrlUpdateCommand{

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
}
