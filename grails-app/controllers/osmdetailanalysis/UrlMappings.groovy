package osmdetailanalysis

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
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
    }
}
