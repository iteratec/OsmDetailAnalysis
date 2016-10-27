package de.iteratec.osm.da.wpt.resolve.exceptions

import de.iteratec.osm.da.fetch.FetchFailReason

class WptResultMissingValueException extends WptResultProcessingException{


    WptResultMissingValueException() {
        super("At least one value within the request was missing")
        fetchFailReason = FetchFailReason.MISSING_VALUES
    }


    @Override
    String getReason() {
        return "At least one value within the requests was missing"
    }
}
