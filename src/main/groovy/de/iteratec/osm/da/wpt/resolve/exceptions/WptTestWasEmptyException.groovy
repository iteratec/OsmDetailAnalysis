package de.iteratec.osm.da.wpt.resolve.exceptions

class WptTestWasEmptyException extends WptResultProcessingException{

    String wptId
    String wptUrl
    String date

    WptTestWasEmptyException(String wptId, String wptUrl) {
        super(setAndGetReason(wptId, wptUrl))
    }

    private String setAndGetReason(String wptId, String wptUrl){
        this.wptUrl = wptUrl
        this.wptId = wptId
        this.date = new Date()
        return getReason()
    }

    @Override
    String getReason() {
        return "The test $wptId from $wptUrl was empty at $date"
    }
}
