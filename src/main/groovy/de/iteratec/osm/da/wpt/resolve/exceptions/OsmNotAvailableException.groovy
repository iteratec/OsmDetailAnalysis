package de.iteratec.osm.da.wpt.resolve.exceptions

class OsmNotAvailableException extends WptResultProcessingException{

    String osmUrl
    Date date

    OsmNotAvailableException(String osmUrl) {
        super(setAndGetReason(osmUrl))
    }

    private String setAndGetReason(String osmUrl){
        date = new Date()
        this.osmUrl = osmUrl
        return getReason()
    }

    @Override
    String getReason() {
        return "Osm $osmUrl wasn't available at $date"
    }
}
