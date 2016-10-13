package de.iteratec.osm.da.wpt.resolve.exceptions

class WptResultMissingValueException extends WptResultProcessingException{

    String missingValue

    WptResultMissingValueException(String missingValue) {
        super(setAndGetReason(missingValue))
    }

    String setAndGetReason(String value){
        missingValue = value
    }

    @Override
    String getReason() {
        return "Atleast the value of $missingValue wasn't set"
    }
}
