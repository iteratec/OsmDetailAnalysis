package de.iteratec.osm.da

import de.iteratec.osm.da.util.UrlUtil

import static groovyx.net.http.ContentTypes.URLENC
import static groovyx.net.http.HttpBuilder.configure

class HttpRequestService {

    private getHttpBuilder(String baseUrl) {
        return configure {
            request.uri = UrlUtil.appendTrailingSlash(baseUrl)
        }
    }

    def getJsonResponse(String baseUrl, String path, Map queryParams = [:]) {
        log.info("getJson: baseUrl=${baseUrl}|path=${path}|queryParams=${queryParams}")
        return getHttpBuilder(baseUrl).get {
            request.uri.path = path.startsWith('/') ? path : "/${path}"
            request.uri.query = queryParams
        }
    }

    def postCallback(String callbackPath, int countAssets, int loadedAssets, int callBackId, String osmUrl, int failureCount) {
        log.info("POST callback to ${osmUrl}${callbackPath}")
        return getHttpBuilder(osmUrl).post {
            request.contentType = URLENC
            request.uri.path = callbackPath.startsWith('/') ? callbackPath : "/${callbackPath}"
            request.uri.query = [countAssets: countAssets, loadedAssets: loadedAssets, callBackId: callBackId, failureCount: failureCount]
        }
    }

}
