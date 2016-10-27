package de.iteratec.osm.da.wpt.resolve.exceptions

import de.iteratec.osm.da.fetch.FetchFailReason
import de.iteratec.osm.da.mapping.OsmDomain

class OsmMappingDoesntExistException extends WptResultProcessingException{

    Map<OsmDomain, Object> missingMappings

    /**
     *
     * @param missingMappings a Map which maps a Domain to either a list of names or a list of ids
     */
    OsmMappingDoesntExistException(Map<OsmDomain, List<Object>> missingMappings) {
        super("Osm Mapping doesn't exist")
        setReason(missingMappings)
        fetchFailReason = FetchFailReason.MAPPINGS_NOT_AVAILABLE
    }

    private String setReason(Map<OsmDomain, List<Object>> missingMappings){
        this.missingMappings = missingMappings
        return getReason()
    }

    @Override
    String getReason() {
        return "OSM couldn't find the mapping: ${missingMappings}"
    }
}
