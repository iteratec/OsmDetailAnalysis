package de.iteratec.osm.da.persistence

import de.iteratec.osm.da.asset.AssetGroup
import grails.gorm.DetachedCriteria

class DbCleanupService {

    public void deleteHarDataBefore(Date toDeleteBefore){
        log.info "begin with deleteResultsDataBefore"

        // use gorm-batching
        def dc = new DetachedCriteria(AssetGroup).build {
            lt 'date', toDeleteBefore.getTime()
        }
        int count = dc.count()

        //batch size -> hibernate doc recommends 10..50
        int batchSize = 50
        //TODO all three delete methods have many things in common,
        0.step(count, batchSize) { int offset ->
            AssetGroup.withNewTransaction {
                dc.list(max: batchSize).each { AssetGroup assetGroup ->
                    try {
                        assetGroup.delete()
                    } catch (Exception e) {
                        println e
                    }
                }
            }
            //clear hibernate session first-level cache
            AssetGroup.withSession { session -> session.clear() }
        }
    }
}
