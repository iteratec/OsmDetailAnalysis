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
    addOnClickListeners();
};

DcDashboard.prototype.clearData = function () {
    dc.filterAll();
    this.allData.remove();
    dc.redrawAll();
    addOnClickListeners();
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
        .on("postRedraw", function () {
            addOnClickListeners();
        })
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
            some: '<strong>%filter-count </strong>' + OsmDetailMicroService.i18n.outOf + '<strong> %total-count </strong>' + OsmDetailMicroService.i18n.records + " " + OsmDetailMicroService.i18n.selected +
            ' | <a href=\'javascript:dc.filterAll(); dc.renderAll();  addOnClickListeners();\'\'>' + OsmDetailMicroService.i18n.resetAll + '</a>',
            all: OsmDetailMicroService.i18n.all + '<strong> %total-count </strong>' + OsmDetailMicroService.i18n.records + " " + OsmDetailMicroService.i18n.selected + '. ' + OsmDetailMicroService.i18n.applyFilters
        });

    dc.renderAll();

    return dataCount;
};

DcDashboard.prototype.addRowChart = function (dashboardIdentifier, chartIdentifier, dimension, group, dataCount, labelAccessor) {
    var chart = dc.rowChart('#' + dashboardIdentifier + ' #' + chartIdentifier);

    chart
        .width(this.dashboardWidth)
        .height(30 * dataCount + 50)
        .on("postRedraw", function () {
            addOnClickListeners();
        })
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
        .elasticX(true)
        .yAxisLabel("ms")
        .legend(dc.legend().x(20).y(700).itemHeight(13).gap(5))
        .x(d3.time.scale().domain([from, to]))
        .xUnits(d3.time.days)
        .compose([]);
    chart.yAxisPadding("5%");
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
            return d.key;
        })
        .valueAccessor(valueAccessor)
        .renderDataPoints({radius: 4, fillOpacity: 0.6, strokeOpacity: 0.8})
        .colors(color)
        .elasticY(true);

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

DcDashboard.prototype.getTimeChart = function (dashboardIdentifier, chartIdentifier) {
    if(!this.timeChart)
        this.timeChart = dc.barChart('#' + dashboardIdentifier + ' #' + chartIdentifier);
    return this.timeChart
};

DcDashboard.prototype.addTimeChart = function (dashboardIdentifier, chartIdentifier, dimension, group, from, to) {
    var chart = this.getTimeChart(dashboardIdentifier, chartIdentifier);
    chart.margins().left = 40;
    chart
        .width(this.dashboardWidth)
        .height(150)
        .x(d3.time.scale().domain([from, to]))
        // .xUnits(d3.time.months)
        .gap(5)
        .elasticX(true)
        .elasticY(true)
        .yAxisLabel("count")
        .brushOn(true)
        .dimension(dimension)
        .group(group)
        .controlsUseVisibility(true);

    chart.yAxis().ticks(8);
    chart.yAxisPadding("5%");



    dc.renderAll();
    this.allDashboardGraphs.push(chart);
    return chart;
};