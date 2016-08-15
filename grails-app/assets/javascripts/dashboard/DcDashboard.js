var DcDashboard = function DcDashboard() {
    // init crossfilter
    this.allData = crossfilter([]);
};

DcDashboard.prototype.addData = function (data) {
    // Parse data at beginning for better performance
    data.forEach(function (d) {
        d.date = new Date(d.epochTimeStarted * 1000);
    });

    this.allData.add(data);
    dc.redrawAll();
};

DcDashboard.prototype.clearData = function () {
    dc.filterAll();
    this.allData.remove();
    dc.redrawAll();
};

DcDashboard.prototype.addPieChart = function (dashboardIdentifier, chartIdentifier, dimension, group, labelAccessor) {
    var chart = dc.pieChart('#' + dashboardIdentifier + ' #' + chartIdentifier);

    chart
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

DcDashboard.prototype.addDataCount = function (dashboardIdentifier, chartIdentifier) {
    var dataCount = dc.dataCount('#' + dashboardIdentifier + ' #' + chartIdentifier);

    dataCount
        .dimension(this.allData)
        .group(this.allData.groupAll())
        .html({
            some: '<strong>%filter-count</strong> selected out of <strong>%total-count</strong> records' +
            ' | <a href=\'javascript:dc.filterAll(); dc.renderAll();\'\'>Reset All</a>',
            all: 'All selected out of <strong>%total-count</strong> records. Please click on the graph to apply filters.'
        });

    dc.renderAll();

    return dataCount;
};

DcDashboard.prototype.addRowChart = function (dashboardIdentifier, chartIdentifier, dimension, group, dataCount, labelAccessor) {
    var chart = dc.rowChart('#' + dashboardIdentifier + ' #' + chartIdentifier);

    chart
        .width(1000)
        .height(30 * dataCount + 50)
        .fixedBarHeight(25)
        .x(d3.scale.linear())
        .elasticX(true)
        .dimension(dimension)
        .group(group)
        .label(labelAccessor)
        .ordering(function (d) {
            return -d.value
        });

    dc.renderAll();

    return chart;
};

DcDashboard.prototype.getCompositeChart = function (dashboardIdentifier, chartIdentifier) {
    if (!this.compositeChart)
        this.compositeChart = dc.compositeChart('#' + dashboardIdentifier + ' #' + chartIdentifier);

    return this.compositeChart
};

DcDashboard.prototype.addCompositeChart = function (dashboardIdentifier, chartIdentifier, from, to, composeArray) {
    var heightOfContainer = 700,
        legendHeight = composeArray.length * (13 + 5);

    var chart = this.getCompositeChart(dashboardIdentifier, chartIdentifier);

    chart.margins().bottom = legendHeight + 20;

    chart
        .width(1200)
        .height(700 + legendHeight)
        .brushOn(false)
        .renderHorizontalGridLines(true)
        .elasticY(true)
        .elasticX(true)
        .legend(dc.legend().x(20).y(700).itemHeight(13).gap(5))
        .x(d3.time.scale().domain([from, to]))
        .xUnits(d3.time.days)

        .compose(composeArray);

    dc.renderAll();

    return chart;
};


DcDashboard.prototype.createLineChart = function (parent, dimension, group, color, label, valueAccessor) {
    return dc.lineChart(parent)
        .dimension(dimension)
        .group(group, label)
        .keyAccessor(function (d) {
            return d.key[0];
        })
        .valueAccessor(valueAccessor)
        .colors(color);
};