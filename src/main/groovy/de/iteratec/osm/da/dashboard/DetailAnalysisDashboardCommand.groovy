package de.iteratec.osm.da.dashboard

import de.iteratec.osm.da.api.ApiKey
import de.iteratec.osm.da.api.OsmCommand
import de.iteratec.osm.da.api.RestApiController
import grails.validation.Validateable
import org.grails.databinding.BindUsing
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

import java.text.SimpleDateFormat
import java.util.regex.Pattern


class DetailAnalysisDashboardCommand extends OsmCommand {
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime()

    /**
     * The selected start date.
     *
     * Please use {@link #createTimeFrameInterval()}.
     */
    @BindUsing({
        obj, source -> source['from'] ? ISO_DATE_TIME_FORMATTER.parseDateTime(source['from'].toString()) : null
    })
    DateTime from

    /**
     * The selected end date.
     *
     * Please use {@link #createTimeFrameInterval()}.
     */
    @BindUsing({
        obj, source -> source['to'] ? ISO_DATE_TIME_FORMATTER.parseDateTime(source['to'].toString()) : null
    })
    DateTime to

    /**
     * A predefined time frame.
     */
    int selectedTimeFrameInterval = 259200

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.schedule.JobGroup CSI groups}
     * which are the systems measured for a CSI value
     */
    Collection<Long> selectedFolder = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.csi.Page pages}
     * which results to be shown.
     */
    Collection<Long> selectedPages = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.result.MeasuredEvent
     * measured events} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllMeasuredEvents} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedMeasuredEventIds = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Browser
     * browsers} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllBrowsers} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedBrowsers = []

    /**
     * The database IDs of the selected {@linkplain de.iteratec.osm.measurement.environment.Location
     * locations} which results to be shown.
     *
     * These selections are only relevant if
     * {@link #selectedAllLocations} is evaluated to
     * <code>false</code>.
     */
    Collection<Long> selectedLocations = []

    Integer bandwidthUp
    Integer bandwidthDown
    Integer latency
    Integer packetloss

    /**
     * Constraints needs to fit.
     */
    static constraints = {
        apiKey(nullable: false, validator: { String currentKey, DetailAnalysisDashboardCommand cmd ->
            List<ApiKey> apiKeys = ApiKey.findAllBySecretKey(currentKey)
            ApiKey validApiKey
            apiKeys.each {
                if (it.osmInstance.domainPath == cmd.domainPath) validApiKey = it
            }
            if (!validApiKey||!validApiKey.allowedToDisplayResults) return [RestApiController.DEFAULT_ACCESS_DENIED_MESSAGE]
            else return true
        })
        from(nullable: true, validator: { DateTime currentFrom, DetailAnalysisDashboardCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentFrom == null) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.from.nullWithManualSelection']
        })
        to(nullable: true, validator: { DateTime currentTo, DetailAnalysisDashboardCommand cmd ->
            boolean manualTimeframe = cmd.selectedTimeFrameInterval == 0
            if (manualTimeframe && currentTo == null) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.to.nullWithManualSelection']
            else if (manualTimeframe && currentTo != null && cmd.from != null && currentTo.isBefore(cmd.from)) return ['de.iteratec.isr.EventResultDashboardController$ShowAllCommand.to.beforeFromDate']
        })

        selectedFolder(nullable: true)
        selectedPages(nullable: true)
        selectedBrowsers(nullable: true)
        selectedMeasuredEventIds(nullable: true)
        selectedLocations(nullable: true)
        bandwidthDown(nullable: true)
        packetloss(nullable: true)
        latency(nullable: true)
        bandwidthUp(nullable: true)
    }

    Interval createTimeFrameInterval() {
        if (this.selectedTimeFrameInterval == 0) {
            return new Interval(this.from, this.to)
        } else {
            DateTime now = DateTime.now()
            return new Interval(now.minusSeconds(this.selectedTimeFrameInterval), now)
        }
    }

    /**
     * <p>
     * Copies all request data to the specified map. This operation does
     * not care about the validation status of this instance.
     * For missing values the defaults are inserted.
     * </p>
     *
     * @param viewModelToCopyTo
     *         The {@link Map} the request data contained in this command
     *         object should be copied to. The map must be modifiable.
     *         Previously contained data will be overwritten.
     *         The argument might not be <code>null</code>.
     */
    void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo) {
        viewModelToCopyTo.put('selectedTimeFrameInterval', this.selectedTimeFrameInterval)
        viewModelToCopyTo.put('from', this.from ? ISO_DATE_TIME_FORMATTER.print(this.from) : null)
        viewModelToCopyTo.put('to', this.to ? ISO_DATE_TIME_FORMATTER.print(this.to) : null)

        viewModelToCopyTo.put('selectedFolder', this.selectedFolder)
        viewModelToCopyTo.put('selectedPages', this.selectedPages)

        viewModelToCopyTo.put('selectedMeasuredEventIds', this.selectedMeasuredEventIds)
        viewModelToCopyTo.put('selectedBrowsers', this.selectedBrowsers)
        viewModelToCopyTo.put('selectedLocations', this.selectedLocations)

        viewModelToCopyTo.put('bandwidthUp', this.bandwidthUp)
        viewModelToCopyTo.put('bandwidthDown', this.bandwidthDown)
        viewModelToCopyTo.put('latency', this.latency)
        viewModelToCopyTo.put('packetloss', this.packetloss)


    }
}
