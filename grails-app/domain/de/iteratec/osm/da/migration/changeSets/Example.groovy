package de.iteratec.osm.da.migration.changeSets

import de.iteratec.osm.da.migration.ChangeSet

//Usage: Has to extend the class ChangeSet and to overwrite the method execute
class Example extends ChangeSet  {

    @Override
    Boolean execute() {
        return true
    }


}
