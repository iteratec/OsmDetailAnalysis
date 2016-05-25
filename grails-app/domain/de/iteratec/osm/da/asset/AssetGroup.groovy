package de.iteratec.osm.da.asset

import de.iteratec.osm.da.instances.OsmInstance

/**
 * AssetGroup
 * A group of assets which shares the same media type and belongs to the same event.
 */
class AssetGroup {

    long osmInstance
    String url
    boolean cached
    String eventName
    String title
    long page
    long jobGroup
    String connectivity
    long jobResult
    long location
    long browser
    //The Mongoplugin maps dates to a epoch time, but as String. So we manually persist it as long
    long date
    long _id

    List<Asset> assets
    static embedded = ['assets']
    static constraints = {
        url nullable: true
        title nullable: true
        eventName nullable: true
    }
}
