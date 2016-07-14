package de.iteratec.osm.da.external.instances

import de.iteratec.oms.da.external.mapping.OSMDomain

/**
 * Representation of an OpenSpeedMonitor-Instance which uses this Service.
 *
 * Example:
 *
 * {
 *     name:OsmDemo
 *     url:demo.openspeedmonitor.org
 *     jobGroupMapping:{
 *         domain: "JobGroup"
 *         mapping: {
 *             1: "A Job"
 *             2: "Another Job"
 *             ...
 *         }
 *     }
 *     locationMapping...
 *     ...
 * }
 *
 */
class OsmInstance {

    String name
    /**
     * URL to the REST-API to get further informations
     */
    String url

    /**
     * For every domain there should be a OsmMapping, which maps ids to names
     */
    OsmMapping jobGroupMapping = new OsmMapping(domain: OSMDomain.JobGroup)
    OsmMapping locationMapping = new OsmMapping(domain: OSMDomain.Location)
    OsmMapping measuredEventMapping = new OsmMapping(domain: OSMDomain.MeasuredEvent)
    OsmMapping browserMapping = new OsmMapping(domain: OSMDomain.Browser)
    OsmMapping pageMapping = new OsmMapping(domain: OSMDomain.Page)
    static embedded = ['jobGroupMapping','locationMapping','measuredEventMapping','browserMapping', 'pageMapping']

    static constraints = {
    }

    /**
     * Get the right map for a OSMDomain
     * @param domain
     * @return OsmMapping
     */
    public OsmMapping getMapping(OSMDomain domain){
        switch (domain){
            case OSMDomain.Browser: return browserMapping
            case OSMDomain.MeasuredEvent: return measuredEventMapping
            case OSMDomain.Location: return locationMapping
            case OSMDomain.JobGroup: return jobGroupMapping
            case OSMDomain.Page: return pageMapping
        }
    }
}
