package de.iteratec.osm.da.api
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

    public RestApiInterceptor(){
        match(controller: "restApi", action: ~/securedViaApiKey.*/)
        match(controller: "detailAnalysisDashboard", action: ~/.*/)
    }

    boolean before() {
        if( params.apiKey == null ) {
            prepareErrorResponse( 403, RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE)
            return false
        }
        ApiKey apiKey = ApiKey.findBySecretKey(params.apiKey)
        if( apiKey == null ) {
            prepareErrorResponse( 403, RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE)
            return false
        }
        if( !apiKey.valid ) {
            prepareErrorResponse( 403, RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE)
            return false
        }
        params['validApiKey'] = apiKey
        return true
    }

    private void prepareErrorResponse( Integer httpStatus, String message) {
        response.setContentType('text/plain;charset=UTF-8')
        response.status=httpStatus

        Writer textOut = new OutputStreamWriter(response.getOutputStream())
        textOut.write(message)
        response.status=httpStatus

        textOut.flush()
        response.getOutputStream().flush()
    }
}
