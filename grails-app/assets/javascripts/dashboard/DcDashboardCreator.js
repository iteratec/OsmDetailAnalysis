//= require DcDashboard.js

function createDashboard(data, labels, from, to) {
    board = new DcDashboard();

    var dataCounts = getDataCounts(data);
    showUniqueValues(dataCounts, data, labels);

    console.log(dataCounts);

    if (dataCounts['browser'] > 1) {
        var browser = board.allData.dimension(function (d) {
            return "" + d['browser'];
        });
        var browserGroup = browser.group().reduceSum(function (d) {
            return d['count']
        });
        var browserLabelAccessor = function (d) {
            return labels['browser'][d.key]
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
        board.addRowChart('dcChart', 'host-chart', host, hostGroup);
    }

    board.addDataCount('dcChart', 'dc-data-count');

    var seriesChartDimension = board.allData.dimension(function (d) {
        return [d.date, d.jobId];
    });

    // Reduce to average
    var loadTimeGroup = seriesChartDimension.group().reduce(
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

    var seriesChartLabelAccessor = function (d) {
        return labels['job'][d.key[1]] + " | LoadTimeInMs";
    };

    board.addSeriesChart('dcChart', 'line-chart', seriesChartDimension, loadTimeGroup, from, to, seriesChartLabelAccessor);

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
        var summary = "<strong>These values are similar for all selected data:</strong> <br/><ul>";

        for (var key in uniqueValues) {
            var label = labels[key] ? labels[key][uniqueValues[key]] : uniqueValues[key];
            summary += "<li>" + key + ": " + label + "</li>";
            var node = document.getElementById(key + "-chart").parentNode;
            node.removeChild(node.childNodes[1]);
        }

        document.getElementById("summary-div").innerHTML = summary + "</ul>";
    }
}

function getDataCounts(data) {
    var result = {};

    var uniqueMap = {'browser': [], 'mediaType': [], 'subtype': [], 'host': [], 'page' : []};
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
    }

    result['browser'] = uniqueMap['browser'].length;
    result['mediaType'] = uniqueMap['mediaType'].length;
    result['subtype'] = uniqueMap['subtype'].length;
    result['host'] = uniqueMap['host'].length;
    result['page'] = uniqueMap['page'].length;

    return result;
}