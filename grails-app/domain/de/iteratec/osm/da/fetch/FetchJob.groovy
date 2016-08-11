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
    //List of all tests which should be persisted
    String wptTestId
    String wptVersion

    static embedded = ['wptTestId']
    static transients = ['currentId']

    static constraints = {
    }

}
