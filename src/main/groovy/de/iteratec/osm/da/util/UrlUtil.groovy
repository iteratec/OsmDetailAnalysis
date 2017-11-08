package de.iteratec.osm.da.util

/**
 * Utils for String represented URL's.
 * @author nkuhn
 */
class UrlUtil {
    /**
     * Appends trailing slash if missing
     * @param url String representation of URL.
     * @return Url with trailing slash appended.
     */
    static String appendTrailingSlash(String url) {
        return url.endsWith('/') ? url : url + '/'
    }

    /**
     * Removes leading hypertext protocols from url if existent.
     * @return Url without hypertext protocols.
     */
    static String removeHypertextProtocols(String url){
        return url.replace('https://','').replace('http://','')
    }
}
