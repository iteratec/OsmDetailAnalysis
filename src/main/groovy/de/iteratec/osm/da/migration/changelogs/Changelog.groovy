package de.iteratec.osm.da.migration.changelogs

import de.iteratec.osm.da.migration.changeSets.DA_V110_2017_08_07
import de.iteratec.osm.da.migration.changeSets.DA_V_1_0_0_MarkoSchnecke_2016_12_23
import de.iteratec.osm.da.migration.changeSets.DA_V110_2017_06_16


//Usage: add an Instance of your changeSet to the end of the "changesets"-list. The name must be unique
//      e.g. new DA_V_1_0_0_MaxMustermann_1970_01_15(),
//           new DA_V_1_0_2_MaxMustermann_1971_01_15(),
//           new DA_V_1_0_2_MaxMustermann_1971_01_15_V2()
class Changelog {
    public static List changesets = [
            new DA_V_1_0_0_MarkoSchnecke_2016_12_23(),
            new DA_V110_2017_06_16(),
            new DA_V110_2017_08_07()
    ]
}
