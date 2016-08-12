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
            action = [PUT: "securedViaApiKeyPersistAssetsForWptResult"]
        }
        "/restApi/showDashboard" {
            controller = "RestApi"
            action = [GET: "securedViaApiKeyShowDashboard"]
        }
    }
}
