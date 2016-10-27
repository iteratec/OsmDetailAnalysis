package de.iteratec.osm.da.wpt.resolve.exceptions

import de.iteratec.osm.da.fetch.FetchFailReason

class WptTestWasEmptyException extends WptResultProcessingException{

    String wptId
    String wptUrl
    String date

    WptTestWasEmptyException(String wptId, String wptUrl) {
        super("Result was Empty")
        setReason(wptId, wptUrl)
        fetchFailReason = FetchFailReason.NO_STEPS_FOUND
    }

    private void setReason(String wptId, String wptUrl){
        this.wptUrl = wptUrl
        this.wptId = wptId
        this.date = new Date()
    }

    @Override
    String getReason() {
        return "The test $wptId from $wptUrl was empty at $date"
    }
}
