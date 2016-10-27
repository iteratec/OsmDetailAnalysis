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
import de.iteratec.osm.da.wpt.resolve.WptDownloadWorker
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
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
        createServices()
        createOsmInstance()
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

    @Betamax(tape="cancelled_devServer01_160810_A7_4D")
    def "Job should fail if wpt result was cancelled"(){
        given: "A FetchJob with no steps"
        createServices()
        createOsmInstance()
        List<FailedFetchJob> failedJobsBefore = FailedFetchJob.list()
        WptDownloadWorker worker = new WptDownloadWorker(service)
        service.addNewFetchJobToQueue(1l,1l,1l,"http://dev.server01.wpt.iteratec.de/",["160810_A7_4D"],"2.19", Priority.Normal)
        service.addExistingFetchJobToQueue([FetchJob.get(1)],Priority.Normal)
        when: "We start the fetching process"
        worker.fetch()

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        List<FailedFetchJob> failedFetchJobDifference = FailedFetchJob.list() - failedJobsBefore
        failedFetchJobDifference.size() == 1
        failedFetchJobDifference[0].reason == FetchFailReason.WPT_TEST_WAS_CANCELLED
    }

    @Betamax(tape="wpt_not_available_devServer01_160810_A7_4D")
    def "Job should fail if wpt was not available"(){
        given: "A FetchJob with no steps"
        createServices()
        createOsmInstance()
        List<FailedFetchJob> failedJobsBefore = FailedFetchJob.list()
        WptDownloadWorker worker = new WptDownloadWorker(service)
        service.addNewFetchJobToQueue(1l,1l,1l,"http://dev.server01.wpt.iteratec.de/",["160810_A7_4D"],"2.19", Priority.Normal)
        service.addExistingFetchJobToQueue([FetchJob.get(1)],Priority.Normal)
        when: "We start the fetching process"
        worker.fetch()

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        List<FailedFetchJob> failedFetchJobDifference = FailedFetchJob.list() - failedJobsBefore
        failedFetchJobDifference.size() == 1
        failedFetchJobDifference[0].reason == FetchFailReason.WPT_NOT_AVAILABLE
    }

    @Betamax(tape="mapping_not_available_devServer01_160810_A7_4D")
    def "Job should fail if osm was not available"(){
        given: "A FetchJob with no steps"
        createServices()
        new OsmInstance(name:"TestInstance", url:"http://localhost:8080").save()
        List<FailedFetchJob> failedJobsBefore = FailedFetchJob.list()
        WptDownloadWorker worker = new WptDownloadWorker(service)
        service.addNewFetchJobToQueue(1l,1l,1l,"http://dev.server01.wpt.iteratec.de/",["160810_A7_4D"],"2.19", Priority.Normal)
        service.addExistingFetchJobToQueue([FetchJob.get(1)],Priority.Normal)
        when: "We start the fetching process"
        worker.fetch()

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        List<FailedFetchJob> failedFetchJobDifference = FailedFetchJob.list() - failedJobsBefore
        failedFetchJobDifference.size() == 1
        failedFetchJobDifference[0].reason == FetchFailReason.MAPPINGS_NOT_AVAILABLE
    }
    @Betamax(tape="test_not_found")
    def "Job should fail if test doesnt exist"(){
        given: "A FetchJob with no steps"
        createServices()
        new OsmInstance(name:"TestInstance", url:"http://localhost:8080").save()
        List<FailedFetchJob> failedJobsBefore = FailedFetchJob.list()
        WptDownloadWorker worker = new WptDownloadWorker(service)
        service.addNewFetchJobToQueue(1l,1l,1l,"http://dev.server01.wpt.iteratec.de/",["160810_A7_4D3"],"2.19", Priority.Normal)
        service.addExistingFetchJobToQueue([FetchJob.get(1)],Priority.Normal)
        when: "We start the fetching process"
        worker.fetch()

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        List<FailedFetchJob> failedFetchJobDifference = FailedFetchJob.list() - failedJobsBefore
        failedFetchJobDifference.size() == 1
        failedFetchJobDifference[0].reason == FetchFailReason.WPT_TEST_ID_DOESNT_EXIST
    }

    @Betamax(tape="invalid_values_devServer01_160810_A7_4D")
    def "Job should fail if values are invalid"(){
        given: "A FetchJob with no steps"
        createServices()
        new OsmInstance(name:"TestInstance", url:"http://localhost:8080").save()
        List<FailedFetchJob> failedJobsBefore = FailedFetchJob.list()
        WptDownloadWorker worker = new WptDownloadWorker(service)
        service.addNewFetchJobToQueue(1l,1l,1l,"http://dev.server01.wpt.iteratec.de/",["160810_A7_4D"],"2.19", Priority.Normal)
        service.addExistingFetchJobToQueue([FetchJob.get(1)],Priority.Normal)
        when: "We start the fetching process"
        worker.fetch()

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        List<FailedFetchJob> failedFetchJobDifference = FailedFetchJob.list() - failedJobsBefore
        failedFetchJobDifference.size() == 1
        failedFetchJobDifference[0].reason == FetchFailReason.MISSING_VALUES
    }

    private OsmInstance createOsmInstance(){
        OsmInstance instance = new OsmInstance(name:"TestInstance", url:"http://localhost:8080")
        instance.browserMapping.mapping.put(1l,"Chrome")
        instance.pageMapping.mapping.put(1l,"undefined")
        instance.measuredEventMapping.mapping.put(1l,"esprit_infrontofotto")
        instance.measuredEventMapping.mapping.put(2l,"google_infrontofotto")
        instance.locationMapping.mapping.put(1l,"iteratec-dev-hetzner-win7:Chrome")
        instance.jobGroupMapping.mapping.put(1l,"JobGroup")
        instance.jobMapping.mapping.put(1l,"Job")
        return instance.save()
    }

    private void createServices(){
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
    }

    private WptDownloadWorker createWorker(){
        WptDetailResultDownloadService downloadService = new WptDetailResultDownloadService(failedFetchJobService: service,
                assetRequestPersistenceService: new AssetRequestPersistenceService(wptDetailResultConvertService:new WptDetailResultConvertService()))
        WptDownloadWorker worker = new WptDownloadWorker(downloadService)
        return worker
    }

}
