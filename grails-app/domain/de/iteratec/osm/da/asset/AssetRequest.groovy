package de.iteratec.osm.da.asset

/**
 * AssetRequest
 * Representation of one single request to load a resource.
 */
class AssetRequest {

    /**
     * Compare values
     */
    int bytesIn
    int bytesOut
    int connectTimeMs
    int downloadTimeMs
    int loadTimeMs
    int timeToFirstByteMs
    //Resource order for one page call
    int indexWithinHar
    int sslNegotiationTimeMs

    /**
     * Grouping values
     */
    String host
    String url
    String mediaType
    String subtype
    String urlWithoutParams

    static mapWith = 'mongo'
    static mapping = {
        bytesIn defaultVaule:-1
        bytesOut defaultVaule:-1
        connectTimeMs defaultVaule:-1
        downloadTimeMs defaultVaule:-1
        indexWithinHar defaultVaule:-1
        loadTimeMs defaultVaule:-1
        sslNegotiationTimeMs defaultVaule:-1
        timeToFirstByteMs defaultVaule:-1
        url defaultVaule: "undefined"
        host defaultVaule: "undefined"
        mediaType defaultVaule: "undefined"
        subtype defaultVaule: "undefined"
        urlWithoutParams defaultVaule: "undefined"
    }

    static constraints = {
        url nullable: true
        host nullable: true
        urlWithoutParams nullable: true
        mediaType nullable: true
        subtype nullable: true
    }
}
