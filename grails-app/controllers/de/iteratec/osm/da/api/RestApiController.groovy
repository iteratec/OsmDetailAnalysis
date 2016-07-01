package de.iteratec.osm.da.api

import de.iteratec.osm.da.har.WptDownloadService
import de.iteratec.osm.da.mapping.MappingService

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
        wptDownloadService.addToQeue(osmInstanceId,command.jobGroupId,command.wptServerBaseUrl,command.wptTestId,command.bandwidthUp,
        command.bandwidthDown, command.latency, command.packetLoss)
        render "Added to Queue"
    }
}

public class PersistenceCommand{

    String osmUrl
    String wptTestId
    String wptServerBaseUrl
    Long jobGroupId
    Integer bandwidthUp
    Integer bandwidthDown
    Integer latency
    Integer packetLoss

    static constraints = {
        osmUrl(nullable:false)
        wptTestId(nullable:false)
        wptServerBaseUrl(nullable:false)
        jobGroupId(nullable:false)
        bandwidthUp(nullable:false)
        bandwidthDown(nullable:false)
        latency(nullable:false)
        packetLoss(nullable:false)
    }


    @Override
    public String toString() {
        return "PersistenceCommand{" +
                "osmUrl='" + osmUrl + '\'' +
                ", wptTestId='" + wptTestId + '\'' +
                ", wptServerBaseUrl='" + wptServerBaseUrl + '\'' +
                ", jobGroupId=" + jobGroupId +
                ", bandwidthUp=" + bandwidthUp +
                ", bandwidthDown=" + bandwidthDown +
                ", latency=" + latency +
                ", packetLoss=" + packetLoss +
                '}';
    }
}
