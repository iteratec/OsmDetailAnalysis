package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.TestDataUtil
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.FailedFetchJob
import de.iteratec.osm.da.fetch.FetchBatch
import de.iteratec.osm.da.fetch.FetchFailReason
import de.iteratec.osm.da.fetch.FetchJob
import de.iteratec.osm.da.fetch.Priority
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.mapping.MappingService
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.wpt.resolve.WptDownloadWorker
import de.iteratec.osm.da.wpt.resolve.exceptions.WptResultMissingValueException
import de.iteratec.osm.da.wpt.resolve.exceptions.WptTestWasEmptyException
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import org.junit.Rule
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import spock.lang.Specification


@TestFor(WptDetailResultDownloadService)
@Mock([OsmInstance, FetchJob, FailedFetchJob, FetchBatch, AssetRequestGroup])
class FetchJobFailTest extends Specification{

    String url = "http://wptTest.openspeedmonitor.org"
    String testId = "163648_BD_4"

    @Rule public RecorderRule recorder = TestDataUtil.getDefaultBetamaxRecorder()

    @Betamax(tape="no_steps_devServer01_160810_A7_4D")
    def "Job should fail if values are missing"(){
        given: "A FetchJob with no steps"
        service.disableWorker()
        service.executor.shutdownNow()
        HttpRequestService httpRequestService = new HttpRequestService()
        TestDataUtil.mockHttpRequestServiceToUseBetamax(httpRequestService)
        service.wptDetailDataStrategyService = new WptDetailDataStrategyService()
        service.failedFetchJobService = new FailedFetchJobService()
        service.wptDetailDataStrategyService = new WptDetailDataStrategyService()
        service.wptDetailDataStrategyService.httpRequestService = httpRequestService
        service.assetRequestPersistenceService = new AssetRequestPersistenceService()
        service.assetRequestPersistenceService.wptDetailResultConvertService = new WptDetailResultConvertService()
        service.assetRequestPersistenceService.wptDetailResultConvertService.mappingService = new MappingService()
        service.assetRequestPersistenceService.wptDetailResultConvertService.mappingService.httpRequestService = httpRequestService
        OsmInstance instance = new OsmInstance(name:"TestInstance", url:"http://localhost:8080").save()
        instance.browserMapping.mapping.put(1l,"Chrome")
        instance.pageMapping.mapping.put(1l,"undefined")
        instance.measuredEventMapping.mapping.put(1l,"esprit_infrontofotto")
        instance.measuredEventMapping.mapping.put(2l,"google_infrontofotto")
        instance.locationMapping.mapping.put(1l,"iteratec-dev-hetzner-win7:Chrome")
        instance.jobGroupMapping.mapping.put(1l,"JobGroup")
        instance.jobMapping.mapping.put(1l,"Job")
        instance.save()
        List<FailedFetchJob> failedJobsBefore = FailedFetchJob.list()
        WptDownloadWorker worker = new WptDownloadWorker(service)
        service.addNewFetchJobToQueue(1l,1l,1l,"http://dev.server01.wpt.iteratec.de/",["160810_A7_4D"],"2.19", Priority.Normal)
        service.addExistingFetchJobToQueue([FetchJob.get(1)],Priority.Normal)
        when: "We start the fetching process"
        worker.fetch()

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        List<FailedFetchJob> failedFetchJobDifference = FailedFetchJob.list() - failedJobsBefore
        failedFetchJobDifference.size() == 1
        failedFetchJobDifference[0].reason == FetchFailReason.NO_STEPS_FOUND
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
