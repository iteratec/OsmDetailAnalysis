package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.fetch.FailedFetchJob
import de.iteratec.osm.da.fetch.FetchFailReason
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.wpt.data.Step
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.wpt.resolve.WptDownloadWorker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Ignore
import spock.lang.Specification

@TestFor(FailedFetchJobService)
@Mock([OsmInstance, FetchJob, FailedFetchJob])
class FetchJobFailTest extends Specification{

    String url = "http://wptTest.openspeedmonitor.org"
    String testId = "163648_BD_4"

    @Ignore //rewrite to a integration test, too many services are involved.
    def "Job should fail if values are missing"(){
        given: "A FetchJob with steps, but missing values"
        WptDownloadWorker wptDownloadWorker = createWorker()
        FetchJob fetchJob = createFetchJob()
        WptDetailResult result = createResult(fetchJob)
        result.location = null
        result.steps = [new Step()]
        int id = fetchJob.id

        when: "We try to handle the result"
        wptDownloadWorker.handleResult(result, fetchJob)

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        FetchJob.get(id) == null
        FailedFetchJob.findByWptBaseURLAndWptTestId(url,testId).reason == FetchFailReason.MISSING_VALUES
    }

    def "Job should fail if there are no steps"(){
        given: "A FetchJob without steps and it's id"
        WptDownloadWorker wptDownloadWorker = createWorker()
        FetchJob fetchJob = createFetchJob()
        WptDetailResult result = createResult(fetchJob)
        int id = fetchJob.id

        when: "We try top handle "
        wptDownloadWorker.handleResult(result, fetchJob)

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        FetchJob.get(id) == null
        FailedFetchJob.findByWptBaseURLAndWptTestId(url,testId).reason == FetchFailReason.NO_STEPS_FOUND
    }

    def "Job should fail if the wpt wasn't available"(){
        given: "A FetchJob without steps and it's id"
        FetchJob fetchJob = createFetchJob()
        WptDetailResultDownloadService downloadService = new WptDetailResultDownloadService(failedFetchJobService: service)

        when: "We fail 3 times to get the result"
        3.times{downloadService.markJobAsFailed(fetchJob)}

        then: "The Job should't exist anymore"
        FetchJob.list().size() == 0
    }

    private FetchJob createFetchJob(){
        return new FetchJob(osmInstance: 1,wptBaseURL: url,
                wptTestId: [testId], jobGroupId: 1, wptVersion: "2.19").save(flush:true, failOnError:true)
    }

    private WptDownloadWorker createWorker(){
        WptDetailResultDownloadService downloadService = new WptDetailResultDownloadService(failedFetchJobService: service,
                assetRequestPersistenceService: new AssetRequestPersistenceService(wptDetailResultConvertService:new WptDetailResultConvertService()))
        WptDownloadWorker worker = new WptDownloadWorker(downloadService)
        return worker
    }

    private static WptDetailResult createResult(FetchJob fetchJob){
        WptDetailResult result = new WptDetailResult(fetchJob)
        result.bandwidthDown = 1
        result.bandwidthUp = 1
        result.latency = 1
        result.packagelossrate= 1
        result.location = "Location"
        result.browser= "FF"
        return result
    }

}
