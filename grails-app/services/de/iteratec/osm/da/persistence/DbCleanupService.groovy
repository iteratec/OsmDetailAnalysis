package de.iteratec.osm.da.persistence

import de.iteratec.osm.da.asset.AssetRequestGroup
import grails.gorm.DetachedCriteria

class DbCleanupService {

    public void deleteHarDataBefore(Date toDeleteBefore){
        log.info "begin with deleteResultsDataBefore"

        // use gorm-batching
        def dc = new DetachedCriteria(AssetRequestGroup).build {
            lt 'date', toDeleteBefore.getTime()
        }
        int count = dc.count()

        //batch size -> hibernate doc recommends 10..50
        int batchSize = 50
        0.step(count, batchSize) { int offset ->
            AssetRequestGroup.withNewTransaction {
                dc.list(max: batchSize).each { AssetRequestGroup assetGroup ->
                    try {
                        assetGroup.delete()
                    } catch (Exception e) {
                        println e
                    }
                }
            }
            //clear hibernate session first-level cache
            AssetRequestGroup.withSession { session -> session.clear() }
        }
    }
}
