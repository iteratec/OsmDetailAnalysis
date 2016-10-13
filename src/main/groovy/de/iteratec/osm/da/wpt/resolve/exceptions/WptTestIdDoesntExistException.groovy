package de.iteratec.osm.da.wpt.resolve.exceptions

class WptTestIdDoesntExistException extends WptResultProcessingException{


    String id
    String wptUrl

    WptTestIdDoesntExistException(String id, String wptUrl) {
        super(setAndGetReason(id, wptUrl))
    }

    private String setAndGetReason(String id, String wptUrl){
        this.id = id
        this.wptUrl = wptUrl
        return getReason()
    }

    @Override
    String getReason() {
        return "The id $id on $wptUrl doesn't exist"
    }
}
