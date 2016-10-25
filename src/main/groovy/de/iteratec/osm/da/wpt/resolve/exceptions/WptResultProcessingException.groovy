package de.iteratec.osm.da.wpt.resolve.exceptions

import de.iteratec.osm.da.fetch.FetchFailReason

abstract class WptResultProcessingException extends RuntimeException{

    FetchFailReason fetchFailReason

    WptResultProcessingException(String s) {
        super(s)
    }

    abstract String getReason()
}
