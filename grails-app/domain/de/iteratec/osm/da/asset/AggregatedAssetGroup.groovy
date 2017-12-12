package de.iteratec.osm.da.asset

class AggregatedAssetGroup {

    long osmInstance
    String wptBaseUrl
    String wptTestId
    long jobGroup
    String mediaType
    long browser
    long epochTimeStarted
    long location
    long measuredEvent
    long page
    Date dateOfPersistence
    List<AggregatedAsset> aggregatedAssets
    static embedded = ['aggregatedAssets']
    static constraints = {
    }
    static mapping = {
        compoundIndex epochTimeStarted:-1, jobGroup:1, page:1, browser:1, location:1, measuredEvent:1
    }
}
