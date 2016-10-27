package de.iteratec.osm.da.fetch

enum FetchFailReason {
        OSM_NOT_AVAILABLE,
        WPT_NOT_AVAILABLE,
        MISSING_VALUES,
        MAPPINGS_NOT_AVAILABLE,
        WPT_TEST_ID_DOESNT_EXIST,
        WPT_TEST_WAS_CANCELLED,
        NO_STEPS_FOUND,
        WPT_VERSION_NOT_SUPPORTED,
        UNKOWN
}
