package de.iteratec.osm.da.har

/**
 * Stores all informations to fetch a HAR from a WPT-Instance and to convert it into the given Assets
 */
class FetchJob {
    
    Date created = new Date()

    long osmInstance
    long jobGroupId
    long jobResultId
    long locationId
    long browserID
    long pageId
    Date jobResultDate
    String url

    static constraints = {
    }
}
