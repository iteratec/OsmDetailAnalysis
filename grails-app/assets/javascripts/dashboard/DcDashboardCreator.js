//= require DcDashboard.js

function createDashboard(data, labels, from, to) {
    board = new DcDashboard();

    var dataCounts = getDataCounts(data);
    var jobs = getJobs(data);
    showUniqueValues(dataCounts, data, labels);

    if (dataCounts['browser'] > 1) {
        var browser = board.allData.dimension(function (d) {
            return "" + d['browser'];
        });
        var browserGroup = browser.group().reduceSum(function (d) {
            return d['count']
        });
        var browserLabelAccessor = function (d) {
            return labels['browser'] ? labels['browser'][d.key] : d.key
        };
        board.addPieChart('dcChart', 'browser-chart', browser, browserGroup, browserLabelAccessor);
    }

    if (dataCounts['mediaType'] > 1) {
        var mediaType = board.allData.dimension(function (d) {
            return "" + d['mediaType'];
        });
        var mediaTypeGroup = mediaType.group().reduceSum(function (d) {
            return d['count']
        });
        var mediaTypeLabelAccessor = function (d) {
            return d.key
        };
        board.addPieChart('dcChart', 'mediaType-chart', mediaType, mediaTypeGroup, mediaTypeLabelAccessor);
    }

    if (dataCounts['subtype'] > 1) {
        var subtype = board.allData.dimension(function (d) {
            return "" + d['subtype'];
        });
        var subtypeGroup = subtype.group().reduceSum(function (d) {
            return d['count'];
        });
        var subtypeLabelAccessor = function (d) {
            return d.key
        };
        board.addPieChart('dcChart', 'subtype-chart', subtype, subtypeGroup, subtypeLabelAccessor);
    }

    if (dataCounts['page'] > 1) {
        var page = board.allData.dimension(function (d) {
            return "" + d['page'];
        });
        var pageGroup = page.group().reduceSum(function (d) {
            return d['count'];
        });
        var pageLabelAccessor = function (d) {
            return labels['page'] ? labels['page'][d.key] : d.key
        };
        board.addPieChart('dcChart', 'page-chart', page, pageGroup, pageLabelAccessor);
    }

    if (dataCounts['measuredEvent'] > 1) {
        var measuredEvent = board.allData.dimension(function (d) {
            return "" + d['measuredEvent'];
        });
        var measuredEventGroup = measuredEvent.group().reduceSum(function (d) {
            return d['count'];
        });
        var measuredEventLabelAccessor = function (d) {
            return labels['measuredEvent'] ? labels['measuredEvent'][d.key] : "" + d.key
        };
        board.addRowChart('dcChart', 'measuredEvent-chart', measuredEvent, measuredEventGroup, dataCounts['measuredEvent'], measuredEventLabelAccessor);
    }

    if (dataCounts['host'] > 1) {
        var host = board.allData.dimension(function (d) {
            return "" + d['host'];
        });
        var hostGroup = host.group().reduceSum(function (d) {
            return d['count'];
        });
        var hostLabelAccessor = function (d) {
            return d.key
        };
        board.addRowChart('dcChart', 'host-chart', host, hostGroup, dataCounts['host'], hostLabelAccessor);
    }

    board.addDataCount('dcChart', 'dc-data-count');

    var seriesChartDimension = board.allData.dimension(function (d) {
        return [d.date, d.jobId];
    });

    var loadTimeGroup_avg = seriesChartDimension.group().reduce(
        //add
        function (p, v) {
            p.count += v['count'];
            p.sum += v['loadTimeMs_avg'] * v['count'];
            p.val = p.count ? d3.round((p.sum / p.count), 2) : 0;
            return p;
        },
        //remove
        function (p, v) {
            p.count -= v['count'];
            p.sum -= v['loadTimeMs_avg'] * v['count'];
            p.val = p.count ? d3.round((p.sum / p.count), 2) : 0;
            return p;
        },
        //init
        function (p, v) {
            return {
                count: 0,
                val: 0,
                sum: 0
            };
        });

    var loadTimeGroup_min = reductio().min(function (d) {
        return +d['loadTimeMs_min']
    })(seriesChartDimension.group());

    var loadTimeGroup_max = reductio().max(function (d) {
        return +d['loadTimeMs_max']
    })(seriesChartDimension.group());


    var composite = board.getCompositeChart('dcChart', 'line-chart');

    var composeArray = [];

    var colorScale = d3.scale.category20c();

    for (var j = 0; j < jobs.length; j++) {
        // avg graph
        var dimension = seriesChartDimension;
        var group = filterJobGroup(loadTimeGroup_avg, jobs[j]);
        var color = colorScale(jobs[j] + "avg");
        var label = (labels['job'] ? labels['job'][jobs[j]] : jobs[j]) + " | LoadTimeMs Avg";
        var valueAccessor = function (d) {
            return d.value.val;
        };
        var lineChart_avg = board.createLineChart(composite, dimension, group, color, label, valueAccessor);

        composeArray.push(lineChart_avg);

        // min graph
        var dimension = seriesChartDimension;
        var group = filterJobGroup(loadTimeGroup_min, jobs[j]);
        var color = colorScale(jobs[j] + "min");
        var label = (labels['job'] ? labels['job'][jobs[j]] : jobs[j]) + " | LoadTimeMs Min";
        var valueAccessor = function (d) {
            if (d.value.min)
                return d.value.min;
            else
                return null;
        };
        var lineChart_min = board.createLineChart(composite, dimension, group, color, label, valueAccessor);

        composeArray.push(lineChart_min);

        // max graph
        var dimension = seriesChartDimension;
        var group = filterJobGroup(loadTimeGroup_max, jobs[j]);
        var color = colorScale(jobs[j] + "max");
        var label = (labels['job'] ? labels['job'][jobs[j]] : jobs[j]) + " | LoadTimeMs Max";
        var valueAccessor = function (d) {
            if (d.value.max)
                return d.value.max;
            else
                return null;
        };
        var lineChart_max = board.createLineChart(composite, dimension, group, color, label, valueAccessor);

        composeArray.push(lineChart_max);
    }

    board.addCompositeChart('dcChart', 'line-chart', from, to, composeArray);


    var p1 = new Promise(
        function (resolve, reject) {
            board.addData(data);
        }
    );
}

function showUniqueValues(dataCounts, data, labels) {
    var uniqueValues = {};

    for (var key in dataCounts) {
        if (dataCounts[key] <= 1) {
            uniqueValues[key] = data[0][key]
        }
    }

    if (Object.keys(uniqueValues).length >= 0) {
        for (var key in uniqueValues) {
            var label = labels[key] ? labels[key][uniqueValues[key]] : uniqueValues[key];
            var summary = "for all values " + key + " is " + label;

            document.getElementById(key + "-chart").innerHTML = summary.toLowerCase()
        }
    }
}

function getDataCounts(data) {
    var result = {};

    var uniqueMap = {'browser': [], 'mediaType': [], 'subtype': [], 'host': [], 'page': [], 'measuredEvent': []};
    for (var i = 0; i < data.length; i++) {
        var datum = data[i];

        if (uniqueMap['browser'].indexOf(datum['browser']) < 0) {
            uniqueMap['browser'].push(datum['browser'])
        }
        if (uniqueMap['mediaType'].indexOf(datum['mediaType']) < 0) {
            uniqueMap['mediaType'].push(datum['mediaType'])
        }
        if (uniqueMap['subtype'].indexOf(datum['subtype']) < 0) {
            uniqueMap['subtype'].push(datum['subtype'])
        }
        if (uniqueMap['host'].indexOf(datum['host']) < 0) {
            uniqueMap['host'].push(datum['host'])
        }
        if (uniqueMap['page'].indexOf(datum['page']) < 0) {
            uniqueMap['page'].push(datum['page'])
        }
        if (uniqueMap['measuredEvent'].indexOf(datum['measuredEvent']) < 0) {
            uniqueMap['measuredEvent'].push(datum['measuredEvent'])
        }
    }

    result['browser'] = uniqueMap['browser'].length;
    result['mediaType'] = uniqueMap['mediaType'].length;
    result['subtype'] = uniqueMap['subtype'].length;
    result['host'] = uniqueMap['host'].length;
    result['page'] = uniqueMap['page'].length;
    result['measuredEvent'] = uniqueMap['measuredEvent'].length;

    return result;
}

function getJobs(data) {
    var result = [];
    for (var i = 0; i < data.length; i++) {
        var currentJob = data[i]['jobId'];
        if (result.indexOf(currentJob) < 0) {
            result.push(currentJob);
        }
    }
    return result;
}

function filterJobGroup(source_group, jobId) {
    function jobIdFilter(d) {
        return d.key[1] == jobId;
    }

    return {
        all: function () {
            return source_group.all().filter(jobIdFilter);
        }
        ,
        top: function (n) {
            return source_group.top(Infinity)
                .filter(jobIdFilter)
                .slice(0, n);
        }
    };
}
