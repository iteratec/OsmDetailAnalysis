package de.iteratec.osm.da.migration


class ChangeSet{

    String uniqueId
    Date executionDate

    def fillAndSave(){
        executionDate = new Date()
        uniqueId = this.class.simpleName
        this.save(failOnError:true)
    }

    Boolean execute() {
        return false
    }


    static constraints = {
        uniqueId unique: true, nullable: false
        executionDate nullable: false
    }

}
