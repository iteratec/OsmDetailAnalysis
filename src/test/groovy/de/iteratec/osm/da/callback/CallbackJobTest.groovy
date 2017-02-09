package de.iteratec.osm.da.callback

import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.fetch.FetchBatch
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.fetch.Priority
import grails.test.mixin.Mock
import org.joda.time.DateTime
import spock.lang.Specification

/**
 * Created by marko on 07.09.16.
 */
@Mock([FetchBatch, FetchJob])
class CallbackJobTest extends Specification {
    CallbackJob callbackJob
    HttpRequestService httpRequestService
    def setup(){
        callbackJob = new CallbackJob()
        httpRequestService = Mock(HttpRequestService)
        callbackJob.httpRequestService = httpRequestService
    }
    def 'Test that invokation of execute does not do anything if there is not FetchBatch'() {
        given: 'There are no FetchBatches'

        when: 'The CronJob is executed'
        callbackJob.execute()

        then: 'Nothing happens'
        0 * httpRequestService.postCallback(_)
    }

    def 'Test that FetchBatches are only processed when queueing is done'() {
        given: 'There is one FetchJob with queueing still in progress'

        FetchBatch fetchBatch = new FetchBatch(callbackUrl:"TestCallbackUrl", osmUrl: "TestOsmUrl", callBackId: 1, countFetchJobs:1).save()
        FetchJob fetchJob = new FetchJob(priority: Priority.Normal, osmInstance: 1, jobId: 1, jobGroupId: 1, wptBaseURL: "http://iteratec.de",
                wptTestId: "abc", wptVersion: "2.19", fetchBatch: fetchBatch).save(flush: true, failOnError: true)

        when: 'The CronJob is executed'
        callbackJob.execute()

        then: 'Nothing happens'
        0 * httpRequestService.postCallback(_)
        FetchBatch.list().size() == 1

        when: "Queueing is done and the CronJob is executed again"
        fetchBatch.queuingDone = true
        fetchBatch.save()
        callbackJob.execute()

        then: 'The request is processed'
        1 * httpRequestService.postCallback('TestCallbackUrl', 1, 1, 1, 'TestOsmUrl', 0)
        FetchBatch.list().size() == 0 // FetchBatch is deleted after it is complete

    }

    def 'Test that the status is only reported if something changed'() {
        given: 'There are two FetchJobs and the queueing is done'

        FetchBatch fetchBatch = new FetchBatch(callbackUrl:"TestCallbackUrl", osmUrl: "TestOsmUrl", callBackId: 1, countFetchJobs:2, lastValue:1,queuingDone:true).save()
        FetchJob fetchJob = new FetchJob(priority: Priority.Normal, osmInstance: 1, jobId: 1, jobGroupId: 1, wptBaseURL: "http://iteratec.de",
                wptTestId: "abc", wptVersion: "2.19", fetchBatch: fetchBatch).save(flush: true, failOnError: true)
        fetchBatch.fetchJobs.add(fetchJob)
        fetchBatch.save()

        when: 'The CronJob is executed'
        callbackJob.execute()

        then: 'Nothing happens'
        0 * httpRequestService.postCallback(_)
        FetchBatch.list().size() == 1

        when: "Last FetchJob is done"
        fetchBatch.fetchJobs.remove(fetchJob)
        fetchBatch.save()
        callbackJob.execute()

        then: 'The request is processed'
        1 * httpRequestService.postCallback('TestCallbackUrl', 2, 2, 1, 'TestOsmUrl', 0)
        FetchBatch.list().size() == 0 // FetchBatch is deleted after it is complete

    }

    def 'Test that the FetchBatch is only deleted after the last FetchJob finished'() {
        given: 'There are two FetchJobs and the queueing is done'

        FetchBatch fetchBatch = new FetchBatch(callbackUrl:"TestCallbackUrl", osmUrl: "TestOsmUrl", callBackId: 1, countFetchJobs:2, lastValue:1,queuingDone:true).save()
        FetchJob fetchJob = new FetchJob(priority: Priority.Normal, osmInstance: 1, jobId: 1, jobGroupId: 1, wptBaseURL: "http://iteratec.de",
                wptTestId: "abc", wptVersion: "2.19", fetchBatch: fetchBatch).save(flush: true, failOnError: true)
        FetchJob fetchJob2 = new FetchJob(priority: Priority.Normal, osmInstance: 1, jobId: 1, jobGroupId: 1, wptBaseURL: "http://iteratec.de",
                wptTestId: "abcd", wptVersion: "2.19", fetchBatch: fetchBatch).save(flush: true, failOnError: true)
        fetchBatch.fetchJobs.add(fetchJob)
        fetchBatch.fetchJobs.add(fetchJob2)
        fetchBatch.save()

        when: 'The CronJob is executed'
        callbackJob.execute()

        then: 'Nothing happens'
        0 * httpRequestService.postCallback(_)
        FetchBatch.list().size() == 1

        when: "Last FetchJob is done"
        fetchBatch.fetchJobs.remove(fetchJob)
        fetchBatch.save()
        callbackJob.execute()

        then: 'The request is processed'
        1 * httpRequestService.postCallback('TestCallbackUrl', 2, 1, 1, 'TestOsmUrl', 0)
        FetchBatch.list().size() == 1 // FetchBatch is not deleted because there is still a FetchJobs left

    }

    def 'Test that the FetchBatch is deactivated after there was no change for 15 min'() {
        given: 'There are two FetchJobs and the queueing is done'

        FetchBatch fetchBatch = new FetchBatch(callbackUrl:"TestCallbackUrl", osmUrl: "TestOsmUrl", callBackId: 1, countFetchJobs:2, lastValue:1,queuingDone:true).save()
        FetchJob fetchJob = new FetchJob(priority: Priority.Normal, osmInstance: 1, jobId: 1, jobGroupId: 1, wptBaseURL: "http://iteratec.de",
                wptTestId: "abc", wptVersion: "2.19", fetchBatch: fetchBatch).save(flush: true, failOnError: true)
        FetchJob fetchJob2 = new FetchJob(priority: Priority.Normal, osmInstance: 1, jobId: 1, jobGroupId: 1, wptBaseURL: "http://iteratec.de",
                wptTestId: "abcd", wptVersion: "2.19", fetchBatch: fetchBatch).save(flush: true, failOnError: true)
        fetchBatch.fetchJobs.add(fetchJob)
        fetchBatch.fetchJobs.add(fetchJob2)
        fetchBatch.save()

        when: 'The CronJob is executed'
        callbackJob.execute()

        then: 'Nothing happens'
        0 * httpRequestService.postCallback(_)
        FetchBatch.list().size() == 1
        fetchBatch.queuingDone == true // job is still active

        when: "Last update was 15 min ago"
        fetchBatch.lastUpdate = new DateTime().minusMinutes(15).toDate()
//        fetchBatch.save()
        callbackJob.execute()

        then: 'The FetchBatch is deactivated'
        0 * httpRequestService.postCallback(_)
        FetchBatch.list().size() == 1 // FetchBatch is not deleted because we might wanna check later what went wrong
        fetchBatch.queuingDone == false

    }



}
