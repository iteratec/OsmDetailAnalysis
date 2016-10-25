package de.iteratec.osm.da.wpt.resolve.exceptions

import de.iteratec.osm.da.fetch.FetchFailReason

class WptTestIdDoesntExistException extends WptResultProcessingException{


    String id
    String wptUrl

    WptTestIdDoesntExistException(String id, String wptUrl) {
        super("WptTest id doesnt exist")
        setReason(id, wptUrl)
        fetchFailReason = FetchFailReason.WPT_TEST_ID_DOESNT_EXIST
    }

    private void setReason(String id, String wptUrl){
        this.id = id
        this.wptUrl = wptUrl
    }

    @Override
    String getReason() {
        return "The id $id on $wptUrl doesn't exist"
    }
}
