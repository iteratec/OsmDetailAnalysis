package de.iteratec.osm.da.callback

import de.iteratec.osm.da.persistence.DbCleanupService

class CleanupJob {
    DbCleanupService dbCleanupService
    static triggers = {
        /**
         * Every Day at 3:00 am
         */
        cron(name: 'DailyOldCsiAggregationsWithDependenciesCleanup', cronExpression: '0 0 3 ? * *')
    }

    def execute(){
        int daysToKeep = grailsApplication?.config?.grails?.de?.iteratec?.osm?.da?.cleanup?.daysToKeep?:-1
        if(daysToKeep == -1) {
            log.debug("Nightly cleanup is disabled or not defined. Skip cleanup")
            return
        }

        Date maxDate = new Date() - daysToKeep
        log.debug("Start nightly cleanup, threshold:  $daysToKeep days")
        dbCleanupService.deleteOldAssetData(maxDate)
    }
}
