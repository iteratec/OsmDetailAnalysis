package de.iteratec.oms.da.external.wpt.data

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class WPTVersion implements Comparable{
    //This map will cache all WPT Versions. If a version isn't valid,
    //the invalid version string will also be cached, so it will return a null on the next call
    static Map<String, WPTVersion> cachedVersions = [:].withDefault {validWPTVersion(it as String)?new WPTVersion(it as String):null}
    static String versionRegex = /\d+\.\d+/
    final int major
    final int minor

    public static WPTVersion get(String version){
        return cachedVersions[version]
    }

    /**
     * This should ony be used to create a cached version
     * @param version
     */
    private WPTVersion(String version){
        String[] split = version.split("\\.")
        //We need the padding because other wise we woulh compare the minor version with a different of digit wrong
        //For example 2.2 would be less than 2.19. So we shift the versions to represent 2 digits -> 2.20
        this.major = Integer.parseInt(split[0])
        this.minor = Integer.parseInt(split[1].padRight(2,"0"))
    }

    static boolean validWPTVersion(String version){
        version =~versionRegex
    }


    @Override
    String toString() {
        return "$major.$minor"
    }

    @Override
    int compareTo(Object o) {
        if(o instanceof WPTVersion){
            if(this.major == o.major){
                return this.minor <=> o.minor
            } else{
                return this.major <=> o.major
            }
        } else{
            return 0
        }
    }
}