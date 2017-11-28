package de.iteratec.osm.da

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ConfigService)
class ConfigServiceSpec extends Specification {

    void "test default MongoDbName"() {
        given: "No MongoDB Configuration"
        service.mongodbConfig.databaseName = null
        expect: "Default Name"
        service.getMongoDbDatabaseName() == service.DEFAULT_MONGODB_DATABASE_NAME
    }

    void "test given MongoDbName"() {
        given: "A name for our database"
        String name = "A Name"
        service.mongodbConfig.databaseName = name
        expect: "The given name"
        service.getMongoDbDatabaseName() == name
    }

    void "test default graphite ServerAddress"() {
        given: "No configured graphite address"
        service.graphiteConfig.serverAddress = null
        expect: "Null, because we doesn't define a default"
        !service.getGraphiteServerAddress()
    }

    void "test given graphite ServerAddress"() {
        given: "A address"
        String address = "https://that.graphite.address"
        service.graphiteConfig.serverAddress = address
        expect: "The given name"
        service.getGraphiteServerAddress() == address
    }

    void "test given graphite port"() {
        given: "A port"
        int port = 222
        service.graphiteConfig.carbonPort = port
        expect: "The given port"
        service.getGraphiteCarbonPort() == port
    }

    void "test missing graphite port"() {
        given: "No port defined"
        service.graphiteConfig.carbonPort = null
        expect: "The default port"
        service.getGraphiteCarbonPort() == service.getDEFAULT_GRAPHITE_PORT()
    }

    void "test missing api keys"(){
        given: "No api keys"
        service.config.da.apiKeys = null
        expect: "An empty list"
        service.getApiKeys() == []
    }

    void "test defined api keys"(){
        given: "A set of api keys"
        def keys = [[key:"abc", osmUrl:"https://that.url.com"],[key:"theOtherKey", url:"https://that.other.url.com"]]
        service.config.da.apiKeys = keys
        expect: "the keys"
        service.getApiKeys() == keys
    }
}
