package de.iteratec.osm.da.persistance

import de.iteratec.osm.da.TestDataUtil
import de.iteratec.osm.da.asset.AssetRequest
import de.iteratec.osm.da.asset.AssetRequestGroup
import de.iteratec.osm.da.instances.OsmInstance
import de.iteratec.osm.da.persistence.AssetRequestPersistenceService
import de.iteratec.osm.da.wpt.WptDetailResultConvertService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Created by marko on 30.12.16.
 */
@TestFor(AssetRequestPersistenceService)
@Mock([AssetRequestGroup, AssetRequest, OsmInstance])
class AssetRequestPersistenceServiceTest extends Specification{
    WptDetailResultConvertService wptDetailResultConvertService
    def setup(){
        wptDetailResultConvertService = Mock(WptDetailResultConvertService)
        service.wptDetailResultConvertService = wptDetailResultConvertService
    }
    def 'Test that getAssetRequestGroups returns a List of AssetRequestGroups'() {
        given: 'There is one AssetRequestGroup'
        AssetRequestGroup assetRequestGroup = TestDataUtil.createAssetRequestGroup()
        def result
        AssetRequestGroup.metaClass.static.findAllByWptBaseUrlAndWptTestIdAndMeasuredEvent = {wptBaseUrl,wptTestId,measuredEvent ->
            result = measuredEvent
            return  [assetRequestGroup]
        }
        when: 'The method is invoked'
        List<AssetRequestGroup> assetRequestGroups = service.getAssetRequestGroups("http://wpt.test.url.de","121212_9R_q0","HP:::JuicyShop_Homepage")
        then: 'It returns the list of AssetRequestGroups'
        assetRequestGroups.size() == 1
        result ==  "JuicyShop_Homepage"
    }

}
