package de.iteratec.osm.da.migration

import de.iteratec.osm.da.migration.changelogs.Changelog
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime

import java.util.concurrent.ExecutionException


class MigrationUtil {
    private static final log = LogFactory.getLog(this)

    def static executeChanges() {
        if (aquireLock()) {
            try {
                Changelog.changesets.each { ChangeSet changeSet ->
                    String nameOfChangeSet = changeSet.class.simpleName
                    log.debug("Processing changeset ${nameOfChangeSet}")
                    if (ChangeSet.findByUniqueId(nameOfChangeSet)) {
                        // If the ChangeSet is already applied we don't want to execute it again
                        log.debug("Changeset already applied ... skipping")
                        return
                    }
                    log.debug("Start executing changeset. Timestamp: ${DateTime.now()} ")
                    def success = changeSet.execute()
                    if (!success) {
                        log.error("Finished executing changeset with an error. Timestamp: ${DateTime.now()}")
                        throw new Exception("Couldn't execute changeSet ${nameOfChangeSet}")
                    }
                    log.debug("Finished executing changeset - persist information about migration. Timestamp: ${DateTime.now()}")
                    changeSet.fillAndSave()
                    log.debug("Successfully applied changeset ${nameOfChangeSet}")
                }
            } finally {
                releaseLock()
            }
        } else {
            throw new Exception("Couldn't aquire the MigrationLock")
        }
    }

    private static boolean aquireLock() {
        def end = new DateTime().plusSeconds(30)
        def success = false
        while (!success && end > DateTime.now()) {
            List<MigrationLock> locks = MigrationLock.list()
            if (locks.empty) {
                new MigrationLock().save(failOnError: true, flush: true)
                continue
            }
            if (locks.size() > 1) throw new Exception("More than one lock found")
            MigrationLock lock = locks[0]
            if (!lock.locked) {
                lock.locked = true
                lock.save(failOnError: true, flush: true)
                success = true

//            }else{
//                Thread.sleep(1000)
            }

        }
        return success
    }

    private static releaseLock() {
        List<MigrationLock> locks = MigrationLock.list()
        if (locks.empty) throw new Exception("No lock found while there has to be one")
        if (locks.size() > 1) throw new Exception("More than one lock found")
        MigrationLock lock = locks[0]
        if (lock.locked) {
            lock.locked = false
            lock.save(failOnError: true, flush: true)
        } else {
            log.error("Couldn't release MigrationLock because it wasn't locked")
        }
    }
}
