package de.iteratec.osm.da.fetch


/**
 * Stores all informations to fetch data from a WPT-Instance and to convert it into the given AssetsRequests
 */
class FetchJob {
    
    Date created = new Date()

    long osmInstance
    long jobId
    long jobGroupId
    String wptBaseURL
    String wptTestId
    String wptVersion

    static constraints = {
    }

}
