package de.iteratec.osm.da.wpt

import de.iteratec.osm.da.fetch.FailedFetchJob
import de.iteratec.osm.da.fetch.FetchJob
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(FailedFetchJobService)
@Mock([FailedFetchJob, FetchJob])
class FailedFetchJobServiceTest {

}
