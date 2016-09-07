package de.iteratec.osm.da.fetch

import spock.lang.Specification

import java.util.concurrent.PriorityBlockingQueue


class FetchJobTest extends Specification {


    def "Test order just by priority"(){
        given: "Three jobs with different priorities"
        FetchJob lowJob = new FetchJob(priority: Priority.Low, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3441",wptVersion:"2.19")
        FetchJob normalJob = new FetchJob(priority: Priority.Normal, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3442",wptVersion:"2.19")
        FetchJob highJob = new FetchJob(priority: Priority.High, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3443",wptVersion:"2.19")
        List<FetchJob> jobs = [lowJob,normalJob,highJob]

        when: "We sort the list of jobs"
        jobs.sort()

        then: "The list should be sorted from low to high priority"
        jobs.get(0) is lowJob
        jobs.get(1) is normalJob
        jobs.get(2) is highJob
    }

    def "Test order just by date"(){
        given: "Three jobs with the same priority, but different dates"
        Date date = new Date()
        FetchJob firstJob = new FetchJob(created: date.plus(2), priority: Priority.Normal, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3441",wptVersion: "2.19")
        FetchJob secondJob = new FetchJob(created:date.plus(1),priority: Priority.Normal, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3442",wptVersion:"2.19")
        FetchJob thirdJob = new FetchJob(created:date, priority: Priority.Normal, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3443",wptVersion:"2.19")
        List<FetchJob> jobs = [secondJob,firstJob,thirdJob]
        when: "We sort the list"
        jobs.sort()

        then: "The list should be sorted from newest date to oldest"
        jobs.get(0) is firstJob
        jobs.get(1) is secondJob
        jobs.get(2) is thirdJob
    }

    def "Test order by priority and date"(){
        given: "Two jobs per priority and each with a different date"
        Date date = new Date()
        FetchJob firstHighJob = new FetchJob(created: date.plus(6), priority: Priority.High, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3441",wptVersion:"2.19")
        FetchJob secondHighJob = new FetchJob(created:date.plus(5),priority: Priority.High, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3442",wptVersion:"2.19")

        FetchJob firstNormalJob = new FetchJob(created:date.plus(2), priority: Priority.Normal, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3443",wptVersion:"2.19")
        FetchJob secondNormalJob = new FetchJob(created:date.plus(1), priority: Priority.Normal, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3444",wptVersion:"2.19")

        FetchJob firstLowJob = new FetchJob(created:date.plus(4), priority: Priority.Low, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3445",wptVersion:"2.19")
        FetchJob secondLowJob = new FetchJob(created:date.plus(3), priority: Priority.Low, osmInstance: 1,jobId: 1,jobGroupId: 1,wptBaseURL: "http://google.de",wptTestId: "CAFE3446",wptVersion:"2.19")

        List<FetchJob> jobs = [firstHighJob, secondHighJob, firstNormalJob, secondNormalJob, firstLowJob, secondLowJob]
        when: "We sort the list"
        jobs.sort()

        then: "The List should be sorted first by priority and then by ascending date"
        jobs.get(0) is firstLowJob
        jobs.get(1) is secondLowJob
        jobs.get(2) is firstNormalJob
        jobs.get(3) is secondNormalJob
        jobs.get(4) is firstHighJob
        jobs.get(5) is secondHighJob
    }

}
