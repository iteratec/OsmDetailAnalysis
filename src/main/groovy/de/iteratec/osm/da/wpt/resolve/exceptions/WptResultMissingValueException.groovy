package de.iteratec.osm.da.wpt.resolve.exceptions

import de.iteratec.osm.da.fetch.FetchFailReason

class WptResultMissingValueException extends WptResultProcessingException{

    String reason


    WptResultMissingValueException(List<String> missingValues ) {
        super("At least one value within the requests was missing")
        StringBuilder builder = new StringBuilder()
        missingValues.each {builder.append(it).append(" + ")}

        reason = "The following values are missing: ${builder.toString()}"
        fetchFailReason = FetchFailReason.MISSING_VALUES
    }


    @Override
    String getReason() {
        return reason
    }
}
