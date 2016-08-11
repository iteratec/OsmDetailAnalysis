//= require DcDashboard.js

function createDashboard(data, labels, from, to) {
    board = new DcDashboard();

    var dataCounts = getDataCounts(data);
    showUniqueValues(dataCounts, data);

    console.log(dataCounts);

    if (dataCounts['browser'] > 1) {
        var browser = board.allData.dimension(function (d) {
            return "" + d['browser'];
        });
        var browserGroup = browser.group();
        var browserLabelAccessor = function (d) {
            return labels['browser'][d.key]
        };
        board.addPieChart('dcChart', 'browser-chart', browser, browserGroup, browserLabelAccessor);
    }

    if (dataCounts['mediaType'] > 1) {
        var mediaType = board.allData.dimension(function (d) {
            return "" + d['mediaType'];
        });
        var mediaTypeGroup = mediaType.group();
        var mediaTypeLabelAccessor = function (d) {
            return d.key
        };
        board.addPieChart('dcChart', 'mediaType-chart', mediaType, mediaTypeGroup, mediaTypeLabelAccessor);
    }

    if (dataCounts['subtype'] > 1) {
        var subtype = board.allData.dimension(function (d) {
            return "" + d['subtype'];
        });
        var subtypeGroup = subtype.group();
        var subtypeLabelAccessor = function (d) {
            return d.key
        };
        board.addPieChart('dcChart', 'subtype-chart', subtype, subtypeGroup, subtypeLabelAccessor);
    }

    board.addDataCount('dcChart', 'dc-data-count');

    var seriesChartDimension = board.allData.dimension(function (d) {
        return [d.date, d.jobId];
    });

    var loadTimeGroup = seriesChartDimension.group().reduceSum(function (d) {
        return d.loadTimeMs;
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

function showUniqueValues(dataCounts, data) {
    var uniqueValues = {};

    for (var key in dataCounts) {
        if (dataCounts[key] <= 1) {
            uniqueValues[key] = data[0][key]
        }
    }

    if (Object.keys(uniqueValues).length >= 0) {
        var summary = "<strong>These values are similar for all selected data:</strong> <br/><ul>";

        for (var key in uniqueValues) {
            summary += "<li>" + key + ": " + uniqueValues[key] + "</li>";
            var node = document.getElementById(key + "-chart").parentNode;
            node.removeChild(node.childNodes[1]);
        }

        document.getElementById("summary-div").innerHTML = summary + "</ul>";
    }
}

function getDataCounts(data) {
    var result = {};

    var uniqueMap = {'browser': [], 'mediaType': [], 'subtype': []};
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
    }

    result['browser'] = uniqueMap['browser'].length;
    result['mediaType'] = uniqueMap['mediaType'].length;
    result['subtype'] = uniqueMap['subtype'].length;

    return result;
}