package de.iteratec.osm.da.instances

import de.iteratec.osm.da.mapping.OsmDomain

/**
 * Representation of an OpenSpeedMonitor-Instance which uses this Service.
 *
 * Example:
 *
 *{*     name:OsmDemo
 *     url:demo.openspeedmonitor.org
 *     jobGroupMapping:{*         domain: "JobGroup"
 *         mapping: {*             1: "A Job"
 *             2: "Another Job"
 *             ...
 *}*}*     locationMapping...
 *     ...
 *}*
 */
class OsmInstance {

    String name
    /**
     * URL to the REST-API to get further informations
     */
    String url

    /**
     * Used as an identifier
     */
    String domainPath

    String protocol

    /**
     * For every domain there should be a OsmMapping, which maps ids to names
     */
    OsmMapping jobGroupMapping = new OsmMapping(domain: OsmDomain.JobGroup)
    OsmMapping locationMapping = new OsmMapping(domain: OsmDomain.Location)
    OsmMapping measuredEventMapping = new OsmMapping(domain: OsmDomain.MeasuredEvent)
    OsmMapping browserMapping = new OsmMapping(domain: OsmDomain.Browser)
    OsmMapping pageMapping = new OsmMapping(domain: OsmDomain.Page)
    OsmMapping jobMapping = new OsmMapping(domain: OsmDomain.Job)
    static embedded = ['jobGroupMapping', 'locationMapping', 'measuredEventMapping', 'browserMapping', 'pageMapping', 'jobMapping']

    static constraints = {
    }

    static String ensureUrlHasTrailingSlash(String url) {
        url = url.endsWith('/') ? url : url + '/'
        return url
    }

    def beforeInsert() {
        this.domainPath = ensureUrlHasTrailingSlash(this.domainPath)
    }

    def beforeUpdate() {
        this.domainPath = ensureUrlHasTrailingSlash(this.domainPath)
    }

    String getUrl(){
        if(protocol) return "$protocol://$domainPath"
        return domainPath
    }

    String setUrl(String url){
        url = ensureUrlHasTrailingSlash(url)
        String path = ""
        String protocol = ""
        switch (url){
            case ~/http:\/\/.+/:
                path = url.replace("http://","")
                protocol = "http"
                break
            case ~/https:\/\/.+/:
                path = url.replace("https://","")
                protocol = "https"
                break
            default:
                path = url
        }
        this.domainPath = path
        this.protocol = protocol
    }

    /**
     * Get the right map for a OsmDomain
     * @param domain
     * @return OsmMapping
     */
    public OsmMapping getMapping(OsmDomain domain) {
        switch (domain) {
            case OsmDomain.Browser: return browserMapping
            case OsmDomain.MeasuredEvent: return measuredEventMapping
            case OsmDomain.Location: return locationMapping
            case OsmDomain.JobGroup: return jobGroupMapping
            case OsmDomain.Page: return pageMapping
            case OsmDomain.Job: return jobMapping
        }
    }
}
