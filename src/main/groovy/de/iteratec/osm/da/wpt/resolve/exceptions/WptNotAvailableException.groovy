package de.iteratec.osm.da.wpt.resolve.exceptions

import de.iteratec.osm.da.fetch.FetchFailReason

class WptNotAvailableException extends WptResultProcessingException{
    String wptUrl
    Date date

    WptNotAvailableException(String wptUrl) {
        super("Wpt wasn't available")
        setReason(wptUrl)
        fetchFailReason = FetchFailReason.WPT_NOT_AVAILABLE
    }

    private String setReason(String osmUrl){
        date = new Date()
        this.wptUrl = osmUrl
        return getReason()
    }

    @Override
    String getReason() {
        return "WPT $wptUrl wasn't available at $date"
    }
}
