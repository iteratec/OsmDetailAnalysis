package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.fetch.Priority
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(WptDetailResultDownloadService)
@Mock([FetchJob, AssetRequestGroup])
class WptDetailResultDownloadServiceTest extends Specification {


    def "Get next job with different priorities"() {
        given: "We disable the worker to run, so they won't 'steal' our jobs"
        service.disableWorker()

        when: "We add three jobs, ech with a own priority"
        service.addNewFetchJobToQueue(1,1,1,"http://iteratec.de",["abc"],"2.19", Priority.Normal)
        service.addNewFetchJobToQueue(1,1,1,"http://iteratec.de",["abcd"],"2.19", Priority.High)
        service.addNewFetchJobToQueue(1,1,1,"http://iteratec.de",["abcde"],"2.19", Priority.Low)

        then: "We should first get the highest priority, then the normal and in the end the lowest"
        service.getNextJob().priority == Priority.High.value
        service.getNextJob().priority == Priority.Normal.value
        service.getNextJob().priority == Priority.Low.value
    }
}
