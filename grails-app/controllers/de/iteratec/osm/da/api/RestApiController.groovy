package de.iteratec.osm.da.api

import de.iteratec.oms.da.external.wpt.data.WPTVersion
import de.iteratec.osm.da.external.wpt.WptDownloadService
import de.iteratec.osm.da.external.mapping.MappingService

class RestApiController {

    MappingService mappingService
    WptDownloadService wptDownloadService


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
