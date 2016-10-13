package de.iteratec.osm.da.wpt.resolve.exceptions

abstract class WptResultProcessingException extends Exception{

    WptResultProcessingException(String s) {
        super(s)
    }

    abstract String getReason()
}
