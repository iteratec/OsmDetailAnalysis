package de.iteratec.osm.da.har

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
