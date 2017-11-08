package de.iteratec.osm.da.report.external

import de.iteratec.osm.da.report.external.provider.GraphiteSocketProvider

class GraphiteServer {

    /**
     * Hostname of this GraphiteServer's carbon component.
     * Graphite carbon component manages storage of metric data
     * (see http://graphite.wikidot.com/high-level-diagram). So this adress is used to send data
     * to graphite server.
     */
    String serverAddress
    /**
     * Port of this GraphiteServer's carbon component.
     * Graphite carbon component manages storage of metric data
     * (see http://graphite.wikidot.com/high-level-diagram). So this port is used to send data
     * to graphite server.
     */
    int port

    GraphiteSocketProvider.Protocol reportProtocol = GraphiteSocketProvider.Protocol.UDP

    static transients = ['serverInetAddress']

    /**
     * Configuration for {@link HealthReportService} with standard values.
     */
    String healthMetricsReportPrefix = "osm.da.healthmetrics"
    String garbageCollectorPrefix = "jvm.gc"
    String memoryReportPrefix = "jvm.mem"
    String threadStatesReportPrefix = "jvm.thread-states"
    String processCpuLoadPrefix = "cpu.processCpuLoad"
    int timeBetweenReportsInSeconds = 300


    public InetAddress getServerInetAddress() {
        return InetAddress.getByName(serverAddress)
    }

    static constraints = {
        serverAddress(unique: 'port', maxSize: 255)
        port(min: 0, max: 65535)
        timeBetweenReportsInSeconds(min: 0, max: 65535)
        healthMetricsReportPrefix(nullable: false, maxSize: 255)
        garbageCollectorPrefix(nullable: false, maxSize: 255)
        memoryReportPrefix(nullable: false, maxSize: 255)
        threadStatesReportPrefix(nullable: false, maxSize: 255)
        reportProtocol(nullable: false, inList: GraphiteSocketProvider.Protocol.values() as List)
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append("GraphiteServer instance object: ${super.toString()}\n")
        sb.append((serverAddress && port) ? "carbon: ${serverAddress}:${port}\n" : '')
        return sb.toString()
    }
}
