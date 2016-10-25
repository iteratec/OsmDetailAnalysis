package de.iteratec.osm.da.wpt.resolve.exceptions

import de.iteratec.osm.da.fetch.FetchFailReason

class WptVersionNotSupportedException extends WptResultProcessingException{

    String wptVersion
    String wptId
    String wptUrl


    WptVersionNotSupportedException(String wptUrl, String wptId, String version) {
        super("Wpt Version was not supported")
        setReason(wptUrl, wptId, version)
        fetchFailReason = FetchFailReason.WPT_VERSION_NOT_SUPPORTED
    }

    void setReason(String wptUrl, String wptId, String version){
        this.wptVersion = version
        this.wptId = wptId
        this.wptUrl = wptUrl
    }

    @Override
    String getReason() {
        return "WPT Version $wptVersion is not supported. Url: $wptUrl id: $wptId"
    }
}
