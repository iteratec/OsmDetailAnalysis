package de.iteratec.osm.da.wpt.resolve.exceptions

class WptNotAvailableException extends WptResultProcessingException{
    String wptUrl
    Date date

    WptNotAvailableException(String wptUrl) {
        super(setAndGetReason(wptUrl))
    }

    private String setAndGetReason(String osmUrl){
        date = new Date()
        this.wptUrl = osmUrl
        return getReason()
    }

    @Override
    String getReason() {
        return "WPT $wptUrl wasn't available at $date"
    }
}
