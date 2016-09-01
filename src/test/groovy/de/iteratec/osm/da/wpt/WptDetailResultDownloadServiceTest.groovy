package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.fetch.Priority
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

import javax.persistence.criteria.Fetch
import java.time.Duration
import java.time.Instant

@TestFor(WptDetailResultDownloadService)
@Mock([FetchJob, AssetRequestGroup])
class WptDetailResultDownloadServiceTest extends Specification {


    def "Get next job with different priorities"() {
        given: "We disable the worker to run, so they won't 'steal' our jobs"
        service.disableWorker()

        when: "We add three jobs, ech with a own priority"
        service.addNewFetchJobToQueue(1, 1, 1, "http://iteratec.de", ["abc"], "2.19", Priority.Normal)
        service.addNewFetchJobToQueue(1, 1, 1, "http://iteratec.de", ["abcd"], "2.19", Priority.High)
        service.addNewFetchJobToQueue(1, 1, 1, "http://iteratec.de", ["abcde"], "2.19", Priority.Low)

        then: "We should first get the highest priority, then the normal and in the end the lowest"
        service.getNextJob().priority == Priority.High.value
        service.getNextJob().priority == Priority.Normal.value
        service.getNextJob().priority == Priority.Low.value
    }

    def "Check if all priorities are available"() {
        when: "We ask fore the priority list"
        def priorities = service.availablePriorities

        then: "There should be Low, Normal and High priority in this list"
        priorities.contains(Priority.Normal)
        priorities.contains(Priority.Low)
        priorities.contains(Priority.High)
    }

    @Unroll("Test that the result for #priority should be #size")
    def "Test if the right list sizes are returned"() {
        given:"For each priority we add some jobs"
        service.disableWorker()
        service.addNewFetchJobToQueue(1, 1, 1, "http://iteratec.de", ["a",], "2.19", Priority.Low)
        service.addNewFetchJobToQueue(1, 1, 1, "http://iteratec.de", ["b", "c"], "2.19", Priority.Normal)
        service.addNewFetchJobToQueue(1, 1, 1, "http://iteratec.de", ["d", "e", "f"], "2.19", Priority.High)

        expect: "There should be the amount returned, which we previously added"
        service.getJobCountInQueueByPriority(priority) == size

        where:
        priority        | size
        Priority.Low    | 1
        Priority.Normal | 2
        Priority.High   | 3
    }

    def "Test that by deleting a result, the result will also be removed from progress "(){
        given: "We add jobs to the queue and safe the amount of current jobs in progress"
        service.disableWorker()
        service.addNewFetchJobToQueue(1, 1, 1, "http://iteratec.de", ["b", "c"], "2.19", Priority.Normal)
        FetchJob job = service.getNextJob()
        int inProgress = service.getInProgress().size()

        when: "We delete a job"
        service.deleteJob(job)

        then: "There should be one job less in progress and the old job shouldn't be in that list"
        service.getInProgress().size() == inProgress-1
        !service.getInProgress().contains(job)
    }
    def "Test that we will receive a low priority job if there is no higher priority"(){
        given:"We add just a low priority job"
        service.disableWorker()
        service.addNewFetchJobToQueue(1, 1, 1, "http://iteratec.de", ["a",], "2.19", Priority.Low)

        when: "We try to get the next job"
        FetchJob job = service.getNextJob()

        then: "We should get a job"
        job
    }

    def "Test that we will receive a normal priority job if there is no higher priority"(){
        given:"We add just a normal priority job"
        service.disableWorker()
        service.addNewFetchJobToQueue(1, 1, 1, "http://iteratec.de", ["a",], "2.19", Priority.Normal)

        when: "We try to get the next job"
        FetchJob job = service.getNextJob()

        then: "We should get a job"
        job
    }

    def "Test that getNextJob will only return, if there is a job available"(){
        given: "We disable the worker to run, so they won't 'steal' our jobs"
        service.disableWorker()
        when: "We add a timer, which will run in 3 seconds"
        new Timer().runAfter(3000){
            service.addNewFetchJobToQueue(1, 1, 1, "http://iteratec.de", ["abc"], "2.19", Priority.Normal)
        }

        then: "We should get the job after minimum of 3 seconds"
        Instant before = Instant.now()
        service.getNextJob()
        Duration.between(before, Instant.now()).getSeconds() >= 2
    }


}