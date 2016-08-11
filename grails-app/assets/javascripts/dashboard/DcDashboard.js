var DcDashboard = function DcDashboard() {
    // init crossfilter
    this.allData = crossfilter([]);
};

DcDashboard.prototype.addData = function (data) {
    // Parse data at beginning for better performance
    data.forEach(function (d) {
        d.date = new Date(d.epochTimeCompleted * 1000);
    });

    this.allData.add(data);
    dc.redrawAll();
};

DcDashboard.prototype.clearData = function () {
    dc.filterAll();
    this.allData.remove();
    dc.redrawAll();
};

DcDashboard.prototype.addPieChart = function (dashbaordIdentifier, chartIdentifier, dimension, group, labelAccessor) {
    var chart = dc.pieChart('#' + dashbaordIdentifier + ' #' + chartIdentifier);

    chart
        .width(180)
        .height(180)
        .radius(80)
        .dimension(dimension)
        .group(group)
        .label(labelAccessor)
        .renderLabel(true)
        .transitionDuration(500)
        .colors(d3.scale.category20c())
        .colorAccessor(function (d, i) {
            return d.key;
        });

    dc.renderAll();

    return chart
};

DcDashboard.prototype.addDataCount = function (dashbaordIdentifier, chartIdentifier) {
  var dataCount =   dc.dataCount('#' + dashbaordIdentifier + ' #' + chartIdentifier);

    dataCount
        .dimension(this.allData)
        .group(this.allData.groupAll())
        .html({
            some: '<strong>%filter-count</strong> selected out of <strong>%total-count</strong> records' +
            ' | <a href=\'javascript:dc.filterAll(); dc.renderAll();\'\'>Reset All</a>',
            all: 'All records selected. Please click on the graph to apply filters.'
        });

    dc.renderAll();

    return dataCount;
};

DcDashboard.prototype.addSeriesChart = function (dashbaordIdentifier, chartIdentifier, dimension, group, from, to, labelAccessor) {
    var seriesChart = dc.seriesChart('#' + dashbaordIdentifier + ' #' + chartIdentifier);

    seriesChart
        .width(990)
        .height(400)
        .transitionDuration(1000)
        .margins({top: 30, right: 50, bottom: 25, left: 60})
        .dimension(dimension)
        .brushOn(false)
        .x(d3.time.scale().domain([from, to]))
        .xUnits(d3.time.days)
        .elasticY(true)
        .elasticX(true)
        .renderHorizontalGridLines(true)
        .legend(dc.legend().x(650).y(50).itemHeight(13).gap(5))
        .group(group)
        .seriesAccessor(labelAccessor)
        .keyAccessor(function (d) {
            return d.key[0];
        })
        .valueAccessor(function (d) {
            return d.value
        });

    dc.renderAll();

    return seriesChart;
};