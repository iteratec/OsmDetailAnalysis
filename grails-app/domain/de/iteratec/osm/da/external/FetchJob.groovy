package de.iteratec.osm.da.external


/**
 * Stores all informations to fetch data from a WPT-Instance and to convert it into the given AssetsRequests
 */
class FetchJob {
    
    Date created = new Date()

    long osmInstance
    long jobGroupId
    String wptBaseURL
    //List of all tests which should be persisted
    List<String> wptTestId
    String wptVersion
    //Next id to fetch
    String currentId

    static embedded = ['wptTestId']
    static transients = ['currentId']

    static constraints = {
    }

    /**
     * Sets the next id from the list to currentId
     * @return true if there was an id, false if the list is empty
     */
    public boolean next(){
        if(wptTestId && wptTestId.size()>0){
            currentId = wptTestId.remove(0)
            return true
        }
        return false
    }
}
