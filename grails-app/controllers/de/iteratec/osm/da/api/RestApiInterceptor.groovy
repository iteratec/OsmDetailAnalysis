/*
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* 	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package de.iteratec.osm.da.api

import de.iteratec.osm.da.util.UrlUtil

/**
 * Checks whether ...
 * <ul>
 *     <li>apiKey is sent via parameter</li>
 *     <li>An apiKey exists with given key value from parameter</li>
 *     <li>Existing apiKey is valid</li>
 * </ul>
 *
 * If one of the checks above fail the subsequent action isn't reached and an error is sent instead.
 */
class RestApiInterceptor {

    public RestApiInterceptor() {
        match(controller: "restApi", action: ~/securedViaApiKey.*/)
//        match(controller: "detailAnalysisDashboard", action: ~/.*/)

    }

    boolean before() {
        if (params.apiKey == null || params.osmUrl == null) {
            log.error("Secured API call failed cause of missing apiKey or osmUrl")
            prepareErrorResponse(response, 403, RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE)
            return false
        }
        List<ApiKey> apiKeys = ApiKey.findAllBySecretKey(params.apiKey)
        if (!apiKeys) {
            log.error("Secured API call failed cause no apiKey was found for given secret")
            prepareErrorResponse(response, 403, RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE)
            return false
        }
        def apiKey
        String domainPath = UrlUtil.appendTrailingSlash(UrlUtil.removeHypertextProtocols(params.osmUrl))
        apiKeys.each {
            log.error("it.osmInstance.domainPath=${it.osmInstance.domainPath}")
            if (it.osmInstance.domainPath == domainPath) {
                apiKey = it
            }
        }
        if (!apiKey) {
            log.error("Secured API call failed cause no apiKey was found for given secret and osmUrl")
            prepareErrorResponse(response, 403, RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE)
            return false
        }
        if (!apiKey.valid) {
            log.error("Secured API call failed cause apiKey is invalid")
            prepareErrorResponse(response, 403, RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE)
            return false
        }
        log.debug("Secured API passed apiKey filter successfully")
        params['validApiKey'] = apiKey
        return true
    }

    private void prepareErrorResponse(javax.servlet.http.HttpServletResponse response, Integer httpStatus, String message) {
        response.setContentType('text/plain;charset=UTF-8')
        response.status = httpStatus

        Writer textOut = new OutputStreamWriter(response.getOutputStream())
        textOut.write(message)
        response.status = httpStatus

        textOut.flush()
        response.getOutputStream().flush()
    }
}
