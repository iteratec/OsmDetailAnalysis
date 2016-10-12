package de.iteratec.osm.da.wpt.resolve

abstract class WptWorker implements Runnable{

    WptWorkerType type
    Date lastTimeStartWaiting
    Date lastEndOfAction
    /**
     * This is used as a counter, so every worker can get a unique id
     */
    private static idCount = 0


    public setWait(){
        lastTimeStartWaiting = new Date()
    }

    public setEndOfAction(){
        lastEndOfAction = new Date()
    }

    /**
     * Get a id for a new worker
     * @return
     */
    private static int nextId(){
        return idCount++
    }
    /**
     * The actual id of this worker
     */
    final id = nextId();

}

enum WptWorkerType {

    WptDownloadWorker("Download Worker"),
    WptQueueFillWorker("Queue Fill Worker")


    private String value

    private WptWorkerType(String value) {
        this.value = value
    }

    @Override
    String toString() {
        return value
    }
}

