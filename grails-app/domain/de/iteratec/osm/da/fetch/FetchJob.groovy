package de.iteratec.osm.da.fetch

import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable


/**
 * Stores all information to fetch data from a WPT-Instance and to convert it into the given AssetsRequests
 */
@EqualsAndHashCode(includes = ['osmInstance', 'wptBaseURL','wptTestId'])
class FetchJob implements Comparable{
    
    Date created = new Date()

    long osmInstance
    long jobId
    long jobGroupId
    String wptBaseURL
    String wptTestId
    String wptVersion
    int tryCount
    long lastTryEpochTime
    int priority

    static constraints = {
    }

    void setPriority(Priority p){
        this.priority = p.value
    }

    void setPriority(int p){
        this.priority = p
    }


    @Override
    public String toString() {
        return "FetchJob{" +
                "id=" + id +
                ", created=" + created +
                ", osmInstance=" + osmInstance +
                ", jobId=" + jobId +
                ", jobGroupId=" + jobGroupId +
                ", wptBaseURL='" + wptBaseURL + '\'' +
                ", wptTestId='" + wptTestId + '\'' +
                ", wptVersion='" + wptVersion + '\'' +
                ", tryCount=" + tryCount +
                ", lastTryEpochTime=" + lastTryEpochTime +
                ", priority=" + priority +
                ", version=" + version +
                '}';
    }

    /**
     * We first sort by priority. If the priority is the same we look for the created date.
     * But the date order will be reversed, because older dates are in a higher priority then newer dates
     * @param o
     * @return
     */
    @Override
    int compareTo(Object o) {
        if(o instanceof FetchJob){
            if(this.priority == o.priority){
                return (this.created <=> o.created) * -1
            } else{
                return this.priority <=> o.priority
            }
        } else{
            return 0
        }
    }


}
