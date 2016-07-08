package de.iteratec.osm.da.asset
/**
 * AssetRequestGroup
 * A group of assets which shares the same media type and belongs to the same event.
 */
class AssetRequestGroup {

    long osmInstance
    String eventName
    long measuredEvent
    long page
    long jobGroup
    @Delegate Connectivity connectivity = new Connectivity()
    long location
    long browser
    long epochTimeCompleted
    String mediaType
    boolean isFirstViewInStep
    long _id

    //To identify if the result is already present
    String wptBaseUrl
    String wptTestId

    List<AssetRequest> assets
    static embedded = ['assets', 'connectivity']
    static constraints = {
    }


}
