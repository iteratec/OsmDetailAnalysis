package osmdetailanalysis

class UrlMappings {

    static mappings = {

        /* Landing page */
        '/' {
            controller = 'StatusRestApi'
            action = [GET: 'getFetchJobStatus']
        }

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "500"(view:'/error')
        "404"(view:'/notFound')

        "/restApi/persistAssetsForWptResult" {
            controller = "RestApi"
            action = [POST: "securedViaApiKeyPersistAssetsForWptResult"]
        }
        "/restApi/updateMapping" {
            controller = "RestApi"
            action = [GET: "securedViaApiKeyUpdateMapping"]
        }
        "/restApi/updateOsmUrl" {
            controller = "RestApi"
            action = [GET: "securedViaApiKeyUpdateOsmUrl"]
        }
        "/restApi/persistAssetsBatchJob" (controller: "RestApi", action: "persistAssetsBatchJob", parseRequest: true)
    }
}
