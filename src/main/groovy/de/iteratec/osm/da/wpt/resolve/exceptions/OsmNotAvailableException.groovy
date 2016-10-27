package de.iteratec.osm.da.wpt.resolve.exceptions

import de.iteratec.osm.da.fetch.FetchFailReason

class OsmNotAvailableException extends WptResultProcessingException{

    String osmUrl
    Date date

    OsmNotAvailableException(String osmUrl) {
        super("Osm wasn't available")
        setReason(osmUrl)
        fetchFailReason = FetchFailReason.OSM_NOT_AVAILABLE
    }

    private String setReason(String osmUrl){
        date = new Date()
        this.osmUrl = osmUrl
        return getReason()
    }

    @Override
    String getReason() {
        return "Osm $osmUrl wasn't available at $date"
    }
}
