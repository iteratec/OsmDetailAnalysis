var DcDashboard = function DcDashboard() {
    // init crossfilter
    this.allData = crossfilter([]);
    this.dashboardWidth = 940;
    this.allDashboardGraphs = [];
};

DcDashboard.prototype.setDashboardWidth = function (width) {
    this.dashboardWidth = width
};

DcDashboard.prototype.addData = function (data) {
    var minDate, maxDate;
    // Parse data at beginning for better performance
    data.forEach(function (d) {
        d.date = new Date(d.epochTimeStarted * 1000);
        if (!minDate || d.date < minDate) {
            minDate = d.date;
        }
        if (!maxDate || d.date > maxDate) {
            maxDate = d.date;
        }
    });

    this.setLineChartDomain('dcChart', 'line-chart', minDate, maxDate);
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
        .radius(90)
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

    this.allDashboardGraphs.push(chart);
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
        .width(this.dashboardWidth)
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

    this.allDashboardGraphs.push(chart);
    return chart;
};

DcDashboard.prototype.getCompositeChart = function (dashboardIdentifier, chartIdentifier) {
    if (!this.compositeChart)
        this.compositeChart = dc.compositeChart('#' + dashboardIdentifier + ' #' + chartIdentifier);

    return this.compositeChart
};

DcDashboard.prototype.addCompositeChart = function (dashboardIdentifier, chartIdentifier, from, to, dataCount) {
    var legendHeight = dataCount * (13 + 5);

    var chart = this.getCompositeChart(dashboardIdentifier, chartIdentifier);

    chart.margins().bottom = legendHeight + 20;
    chart.margins().left = 40;

    chart
        .width(this.dashboardWidth)
        .height(700 + legendHeight)
        .brushOn(false)
        .renderHorizontalGridLines(true)
        .elasticY(true)
        .yAxisLabel("ms")
        .legend(dc.legend().x(20).y(700).itemHeight(13).gap(5))
        .x(d3.time.scale().domain([from, to]))
        .xUnits(d3.time.days)
        .compose([]);

    dc.renderAll();

    this.allDashboardGraphs.push(chart);
    return chart;
};

DcDashboard.prototype.setLineChartDomain = function (dashboardIdentifier, chartIdentifier, from, to) {
    this.getCompositeChart(dashboardIdentifier, chartIdentifier).x(d3.time.scale().domain([from, to]))
};


DcDashboard.prototype.createLineChart = function (parent, dimension, group, color, label, valueAccessor, unit) {
    var chart = dc.lineChart(parent)
        .dimension(dimension)
        .group(group, label)
        .keyAccessor(function (d) {
            return d.key[0];
        })
        .valueAccessor(valueAccessor)
        .renderDataPoints({radius: 2, fillOpacity: 0.6, strokeOpacity: 0.8})
        .colors(color);

    if(unit == "bytes"){
        chart.useRightYAxis(true);
    }

    this.allDashboardGraphs.push(chart);
    return chart;
};

DcDashboard.prototype.setAnimationTime = function (time) {
    for(var i = 0; i < this.allDashboardGraphs.length; i++) {
        this.allDashboardGraphs[i].transitionDuration(time);
    }
};