package de.iteratec.osm.da

import groovy.json.JsonBuilder
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class HttpRequestService {

    private Map<String, RESTClient> clients = new HashMap()

    Object getHTTPResponse(String baseUrl, String path, Map query, ContentType contentType, Map headers){
        getRestClient(baseUrl).get(
                path: path,
                query: query,
                contentType: contentType,
                headers: headers
        )
    }
    GPathResult getXMLResponseAsGPathResult(String baseUrl, String path, Map query, ContentType contentType, Map headers){
        return parseXml(getHTTPResponse(baseUrl, path, query, contentType, headers))
    }
    GPathResult parseXml(Object resp) {
        assert resp != null

        def myresponse = new XmlSlurper().parseText(resp.data.text)
        assert myresponse != null

        return myresponse
    }

    RESTClient getRestClient(String url) {
        return new RESTClient(url)
    }
    RESTClient getRestClientCached(String baseUrl) {
        assert baseUrl != null

        RESTClient client = this.clients[baseUrl]
        if(!client) {
            client = getRestClient(baseUrl)
            this.clients[baseUrl] = client
        }
        return client
    }

    def getJsonResponseFromOsm(String baseUrl, String path, Map queryParams){
        RESTClient client = getRestClient(baseUrl)
        String json = new JsonBuilder(queryParams).toString()
        def response = client.get(
                path: path+json,
                contentType: ContentType.JSON,
                headers : [Accept : 'application/json']
        )
        return response.data
    }

    def getJsonResponse(String baseUrl, String path, queryParams){
        log.debug("Trying to get json. baseUrl=${baseUrl}, path=${path}, queryParams=${queryParams}")
        RESTClient client = getRestClient(baseUrl)
        def response = client.get(
                path: path,
                query: queryParams,
                contentType: ContentType.JSON,
                headers : [Accept : 'application/json']
        )
        log.debug("... DONE")
        return response.data
    }

    def postCallback(String callbackUrl, int countAssets, int loadedAssets,int callBackId,String osmUrl, int failureCount){
        RESTClient client = getRestClient(osmUrl)

        def response = client.post(
            path: callbackUrl,
            query: [countAssets: countAssets, loadedAssets: loadedAssets,callBackId:callBackId,failureCount:failureCount],
            contentType: ContentType.URLENC
        )
        return response
    }

    /**
     * Turn a String representation of the query from a URL into a map of parameter
     * which can be used with other Methods from this wptDetailResultDownloadService
     * @param query
     * @return
     */
    Map splitQueryStringToMap(String query){
        def map = [:]
        query.split("&").each {keyValue ->
            keyValue.split("=").with {map[it[0]] = it[1]}
        }
        return map
    }
}
