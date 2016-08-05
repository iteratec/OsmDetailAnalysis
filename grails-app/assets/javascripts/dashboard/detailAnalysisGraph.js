function drawDcGraph(data, labelAliases, from, to, graphIdentifier) {

    // Defining charts
    var browserChart = dc.pieChart('#' + graphIdentifier +  ' #browser-chart');
    var mediaTypeChart = dc.pieChart('#' + graphIdentifier +  ' #media-type-chart');
    var subTypeChart = dc.pieChart('#' + graphIdentifier +  ' #subtype-chart');
    var lineChart = dc.lineChart('#' + graphIdentifier +  ' #line-chart');
    var dataCount = dc.dataCount('#' + graphIdentifier +  ' #dc-data-count');

    // Parse data at beginning for better performance
    data.forEach(function (d) {
        d.date = new Date(d.epochTimeCompleted * 1000);
    });

    var minDate = from;
    var maxDate = to;

    var allData = crossfilter(data);
    var allDataGroup = allData.groupAll();

    // Define Dimenstions and Groups
    var mediaType = allData.dimension(function (d) {
        return d.mediaType;
    });
    var mediaTypeGroup = mediaType.group();

    var subtype = allData.dimension(function (d) {
        return d.subtype;
    });
    var subtypeGroup = subtype.group();

    var browser = allData.dimension(function (d) {
        return d.browser;
    });
    var browserGroup = browser.group();

    var dataDate = allData.dimension(function (d) {
        return d.date;
    });
    var ttfsGroup = dataDate.group().reduceSum(function (d) {
        return d.timeToFirstByteMs;
    });

    var loadTimeGroup = dataDate.group().reduceSum(function (d) {
        return d.loadTimeMs;
    });

    // Define Charts and Layouts
    browserChart
        .width(180)
        .height(180)
        .radius(80)
        .dimension(browser)
        .group(browserGroup)
        .label(function (d) {
            return labelAliases['browser'][d.key];
        })
        .renderLabel(true)
        .transitionDuration(500)
        .colors(d3.scale.category20c())
        .colorDomain([-1750, 1644])
        .colorAccessor(function (d, i) {
            return d.key;
        });

    mediaTypeChart
        .width(180)
        .height(180)
        .radius(80)
        .dimension(mediaType)
        .group(mediaTypeGroup)
        .label(function (d) {
            return d.key
        })
        .renderLabel(true)
        .transitionDuration(500)
        .colors(d3.scale.category20c())
        .colorDomain([-1750, 1644])
        .colorAccessor(function (d, i) {
            return d.key;
        });

    subTypeChart
        .width(180)
        .height(180)
        .radius(80)
        .dimension(subtype)
        .group(subtypeGroup)
        .label(function (d) {
            return d.key
        })
        .renderLabel(true)
        .transitionDuration(500)
        .colors(d3.scale.category20c())
        .colorDomain([-1750, 1644])
        .colorAccessor(function (d, i) {
            return d.key;
        });

    lineChart
        .width(990)
        .height(200)
        .transitionDuration(1000)
        .margins({top: 30, right: 50, bottom: 25, left: 60})
        .dimension(dataDate)
        .x(d3.time.scale().domain([minDate, maxDate]))
        .xUnits(d3.time.days)
        .elasticY(true)
        .elasticX(true)
        .renderHorizontalGridLines(true)
        .legend(dc.legend().x(800).y(10).itemHeight(13).gap(5))
        .group(ttfsGroup, 'Time to first Byte')
        .valueAccessor(function (d) {
            return d.value;
        })
        .stack(loadTimeGroup, 'Load Time in Ms', function (d) {
            return d.value;
        });

    dataCount
        .dimension(allData)
        .group(allDataGroup)
        .html({
            some: '<strong>%filter-count</strong> selected out of <strong>%total-count</strong> records' +
            ' | <a href=\'javascript:dc.filterAll(); dc.renderAll();\'\'>Reset All</a>',
            all: 'All records selected. Please click on the graph to apply filters.'
        });


    // // Drawing everything
    dc.renderAll();
    dc.redrawAll();
}