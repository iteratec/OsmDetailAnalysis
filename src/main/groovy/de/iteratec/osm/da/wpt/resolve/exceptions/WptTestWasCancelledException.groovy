package de.iteratec.osm.da.wpt.resolve.exceptions

class WptTestWasCancelledException extends WptTestIdDoesntExistException{

    WptTestWasCancelledException(String id, String wptUrl) {
        super(id, wptUrl)
    }

    @Override
    String getReason() {
        return "The id $id on $wptUrl was cancelled"
    }
}
