package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.HttpRequestService
import de.iteratec.osm.da.asset.AggregatedAssetGroup
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.fetch.*
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.mapping.MappingService
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.wpt.data.WptDetailResult
import de.iteratec.osm.da.wpt.resolve.WptDetailDataDefaultStrategy
import de.iteratec.osm.da.wpt.resolve.exceptions.OsmMappingDoesntExistException
import de.iteratec.osm.da.wpt.resolve.exceptions.WptNotAvailableException
import de.iteratec.osm.da.wpt.resolve.exceptions.WptResultMissingValueException
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import static de.iteratec.osm.da.fetch.FetchFailReason.*
@Unroll
@TestFor(WptDetailResultDownloadService)
@Mock([OsmInstance, FetchJob, FailedFetchJob, FetchBatch, AssetRequestGroup, AggregatedAssetGroup])
class FetchJobFailTest extends Specification{

    def setup(){
        createServicesCommonToAllTests()
        createOsmInstance()
    }

    def "Job should fail for json response #resultJsonFile with reason #fetchFailReason"(){
    @Ignore("Fails because of this: https://github.com/betamaxteam/betamax/issues/232")
        given: "A FetchJob with no steps"
        int failedJobsBefore = FailedFetchJob.list().size()
        service.createNewFetchJob(1l,1l,1l,"http://dev.server01.wpt.iteratec.de/",["160810_A7_4D"],"2.19", Priority.Normal)
        service.addNewFetchJobToQueue(
            1l,
            1l,
            1l,
            "http://dev.server01.wpt.iteratec.de/",
            ["160810_A7_4D"],
            "2.19",
            Priority.Normal
        )
        service.wptDetailDataStrategyService = Stub(WptDetailDataStrategyService){
            getStrategyForVersion(_) >> new WptDetailDataDefaultStrategy(
                httpRequestService: Stub(HttpRequestService){
                    getJsonResponse(_, _, _) >> new JsonSlurper().parse(new File("src/test/resources/jsonResponses/${resultJsonFile}.json"))
                }
            )
        }

        when: "We start the fetching process"
        wptDownloadTask.run()

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        List<FailedFetchJob> failedFetchJobs = FailedFetchJob.list()
        failedFetchJobs.size()  - failedJobsBefore == 1
        failedFetchJobs[0].reason == fetchFailReason
    @Ignore("Fails because of this: https://github.com/betamaxteam/betamax/issues/232")

        where:
        resultJsonFile                              || fetchFailReason
        'no_steps_webpagetest.org_170616_J5_1AC2'   || NO_STEPS_FOUND
        'wptResultCanceled'                         || WPT_TEST_WAS_CANCELLED
        'test_not_found'                            || WPT_TEST_ID_DOESNT_EXIST
    }

    @Ignore("Fails because of this: https://github.com/betamaxteam/betamax/issues/232")
    def "Job should fail if wpt was not available"(){
        given: "A FetchJob with no steps"
        List<FailedFetchJob> failedJobsBefore = FailedFetchJob.list()
        service.createNewFetchJob(1l,1l,1l,"http://dev.server01.wpt.iteratec.de/",["160810_A7_4D"],"2.19", Priority.Normal)
        service.addNewFetchJobToQueue(
            1l,
            1l,
            1l,
            "http://dev.server01.wpt.iteratec.de/",
            ["160810_A7_4D"],
            "2.19",
            Priority.Normal
        )
        service.wptDetailDataStrategyService = Spy(WptDetailDataStrategyService){
            getStrategyForVersion(_) >> Spy(WptDetailDataDefaultStrategy){
                loadJson(_, _) >> {FetchJob fetchJob, int tries = 0 ->
                    throw new WptNotAvailableException("http://dev.server01.wpt.iteratec.de/")
                }
            }
        }
        when: "We start the fetching process"
        wptDownloadTask.run()

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        List<FailedFetchJob> failedFetchJobDifference = FailedFetchJob.list() - failedJobsBefore
        failedFetchJobDifference.size() == 1
        failedFetchJobDifference[0].reason == WPT_NOT_AVAILABLE
    }

    def "Job should fail if osm was not available"(){
        given: "A regular FetchJob but mocked method to save detail data throws OsmMappingDoesntExistException"
        new OsmInstance(name:"TestInstance", url:"http://localhost:8080").save()
        List<FailedFetchJob> failedJobsBefore = FailedFetchJob.list()
        service.createNewFetchJob(1l,1l,1l,"http://dev.server01.wpt.iteratec.de/",["160810_A7_4D"],"2.19", Priority.Normal)
        service.addNewFetchJobToQueue(
            1l,
            1l,
            1l,
            "http://dev.server01.wpt.iteratec.de/",
            ["160810_A7_4D"],
            "2.19",
            Priority.Normal
        )
        service.assetRequestPersistenceService = Spy(AssetRequestPersistenceService){
            saveDetailDataForJobResult(_, _) >> {WptDetailResult result, FetchJob fetchJob ->
                throw new OsmMappingDoesntExistException()
        FetchJob.count() == 0
            }
        }

        when: "We start the fetching process"
        wptDownloadTask.run()

        then: "The FetchJob should't exist anymore and a FailedFetchJob with the correct reason should exist"
        List<FailedFetchJob> failedFetchJobDifference = FailedFetchJob.list() - failedJobsBefore
        failedFetchJobDifference.size() == 1
        failedFetchJobDifference[0].reason == FetchFailReason.MAPPINGS_NOT_AVAILABLE
    }

    @Ignore("Fails because of this: https://github.com/betamaxteam/betamax/issues/232")
    def "Job should fail if values are invalid"(){
        given: "A FetchJob with no steps"
        new OsmInstance(name:"TestInstance", url:"http://localhost:8080").save()
        List<FailedFetchJob> failedJobsBefore = FailedFetchJob.list()
        service.createNewFetchJob(1l,1l,1l,"http://dev.server01.wpt.iteratec.de/",["160810_A7_4D"],"2.19", Priority.Normal)
        service.addNewFetchJobToQueue(
            1l,
            1l,
            1l,
            "http://dev.server01.wpt.iteratec.de/",
            ["160810_A7_4D"],
            "2.19",
            Priority.Normal
        )
        service.wptDetailDataStrategyService = Spy(WptDetailDataStrategyService){
            getStrategyForVersion(_) >> Spy(WptDetailDataDefaultStrategy){
                getResult(_) >> { FetchJob fetchJob ->
                    def missingValueStringList = ["missingWptValue"]
                    throw new WptResultMissingValueException(missingValueStringList)
                }
            }
        }
        when: "We start the fetching process"
        wptDownloadTask.run()

        then: "The Job should't exist anymore and a FailedFetchJob with the correct reason should exist"
        List<FailedFetchJob> failedFetchJobDifference = FailedFetchJob.list() - failedJobsBefore
        failedFetchJobDifference.size() == 1
        FetchJob.count() == 0
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

    private void createServicesCommonToAllTests(){
        service.wptDetailDataStrategyService = new WptDetailDataStrategyService()
        service.failedFetchJobService = new FailedFetchJobService()
        service.wptDetailDataStrategyService = new WptDetailDataStrategyService()
        service.assetRequestPersistenceService = new AssetRequestPersistenceService()
        service.assetRequestPersistenceService.wptDetailResultConvertService = new WptDetailResultConvertService()
        service.assetRequestPersistenceService.wptDetailResultConvertService.mappingService = new MappingService()
        WptDetailResultDownloadService.MAX_TRY_COUNT = 1
        HttpRequestService httpRequestService = new HttpRequestService()
        service.wptDetailDataStrategyService.httpRequestService = httpRequestService
        service.assetRequestPersistenceService.wptDetailResultConvertService.mappingService.httpRequestService = httpRequestService
    }
}
