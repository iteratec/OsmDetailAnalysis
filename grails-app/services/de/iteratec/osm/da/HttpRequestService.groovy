package de.iteratec.osm.da

import static groovyx.net.http.ContentTypes.URLENC
import static groovyx.net.http.ContentTypes.JSON
import static groovyx.net.http.HttpBuilder.configure

class HttpRequestService {

    private getHttpBuilder(String baseUrl) {
        return configure {
            request.uri = baseUrl
        }
    }

    def getJsonResponse(String baseUrl, String path, Map queryParams = [:]) {
        return getHttpBuilder(baseUrl).get {
            request.contentType = JSON
            request.headers['Accept'] = 'application/json'
            request.uri.path = path
            request.uri.query = queryParams
        }
    }

    def postCallback(String callbackUrl, int countAssets, int loadedAssets, int callBackId, String osmUrl, int failureCount) {
        return configure {
            request.uri = callbackUrl
            request.contentType = URLENC
        }.post {
            request.uri.query = [countAssets: countAssets, loadedAssets: loadedAssets, callBackId: callBackId, failureCount: failureCount]
        }
    }

}
