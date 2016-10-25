package de.iteratec.osm.da.wpt.resolve.exceptions

import de.iteratec.osm.da.fetch.FetchFailReason

class WptTestWasCancelledException extends WptTestIdDoesntExistException{

    WptTestWasCancelledException(String id, String wptUrl) {
        super(id, wptUrl)
        fetchFailReason = FetchFailReason.WPT_TEST_WAS_CANCELLED
    }

    @Override
    String getReason() {
        return "The id $id on $wptUrl was cancelled"
    }
}
