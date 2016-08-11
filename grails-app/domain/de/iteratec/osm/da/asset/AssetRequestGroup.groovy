package de.iteratec.osm.da.asset
/**
 * AssetRequestGroup
 * A group of assets which shares the same media type and belongs to the same step.
 *
 * Example of a multi step test:
 * First View:
 *  step1:
 *      5 Image
 *      2 Text
 *      1 css
 *  step2:
 *      2 Images
 *      1 Text
 * Second View:
 *  step1:
 *    2 Image
 *      2 Text
 *      1 css
 *  step2:
 *      2 Images
 *      1 Text
 *
 * Step 1 will result in 3 AssetRequestGroups, Step 2 in 2 Results. The same applies for the second view. Overall there will be 9 AssetGroups.
 *
 */
class AssetRequestGroup {

    long osmInstance
    String eventName
    long measuredEvent
    long page
    long jobId
    long jobGroup
    @Delegate Connectivity connectivity = new Connectivity()
    long location
    long browser
    long epochTimeStarted
    String mediaType
    boolean isFirstViewInStep

    //To identify if the result is already present
    String wptBaseUrl
    String wptTestId

    List<AssetRequest> assets
    static embedded = ['assets', 'connectivity']
    static constraints = {
    }


}
