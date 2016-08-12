package de.iteratec.osm.da.api

/**
 * Used to secure api functions. Contains one boolean for each api function or group of functions that has to be secured.
 * These booleans should only be used directly
 * @author markoschnecke
 * @see ApiSecurityService
 */
class ApiKey {

    String secretKey
    String description
    Boolean valid = true
    Boolean allowedToTriggerFetchJobs = false
    Boolean allowedToDisplayResults = false


    static mapping = {
        valid(defaultValue: true)
        allowedToTriggerFetchJobs(defaultValue: false)
    }

    static constraints = {
        secretKey(nullable: false, blank: false)
        description(nullable: true)
        valid(nullable: false)
        allowedToTriggerFetchJobs(nullable: false)
    }
}
