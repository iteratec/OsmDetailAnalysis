//= require_tree ../bower_components/jquery
//= require_tree ../bower_components/jquery-ui
//= require_tree ../bower_components/crossfilter2
//= require_tree ../bower_components/d3
//= require_tree ../bower_components/dcjs
//= require_tree ../bower_components/reductio
//= require DcDashboard.js

function createDashboard(data, labels, from, to) {
    board = new DcDashboard();
    // Set dashboard width same as div width
    var width = $(".dashboardContainer").css("width").replace("px", "");
    board.setDashboardWidth(+width);

    var dataCounts = getDataCounts(data);
    var jobs = getJobs(data);
    showUniqueValues(dataCounts, data, labels);

    // BROWSER FILTER
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

    // MEDIATYPE FILTER
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

    // SUBTYPE FILTER
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

    // PAGE FILTER
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

    //JOBGROUP FILTER
    if (dataCounts['jobGroup'] > 1){
        var jobGroup = board.allData.dimension(function (d) {
            return "" + d['jobGroup']
        });
        var jobGroupGroup = jobGroup.group().reduceSum(function (d) {
            return d['count'];
        });
        var jobGroupLabelAccessor = function (d) {
            if (d.key < 0) {
                return "undefined"
            }
            return labels['jobGroup'] ? labels['jobGroup'][d.key] : "" + d.key
        };
        board.addPieChart('dcChart', 'jobGroup-chart', jobGroup, jobGroupGroup, jobGroupLabelAccessor)
    }

    // MEASURED EVENT FILTER
    if (dataCounts['measuredEvent'] > 1) {
        var measuredEvent = board.allData.dimension(function (d) {
            return "" + d['measuredEvent'];
        });
        var measuredEventGroup = measuredEvent.group().reduceSum(function (d) {
            return d['count'];
        });
        var measuredEventLabelAccessor = function (d) {
            if (d.key < 0) {
                return "undefined"
            }
            return labels['measuredEvent'] ? labels['measuredEvent'][d.key] : "" + d.key
        };
        board.addRowChart('dcChart', 'measuredEvent-chart', measuredEvent, measuredEventGroup, dataCounts['measuredEvent'], measuredEventLabelAccessor);
    }

    // HOST FILTER
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


    // Show count of selected data
    board.addDataCount('dcChart', 'dc-data-count');

    // ### BEGIN LINE CHARTS
    var jobId_Date_Dimension = board.allData.dimension(function (d) {
        return [d.date, d.jobId];
    });

    var loadTime_ttfb_avg = jobId_Date_Dimension.group().reduce(
        //add
        function (p, v) {
            p.count += v['count'];
            p.loadTimeSum += v['loadTimeMs_avg'] * v['count'];
            p.loadTimeAvg = p.count ? d3.round((p.loadTimeSum / p.count), 2) : 0;
            p.ttfbSum += v['ttfb_avg'] * v['count'];
            p.ttfbAvg = p.count ? d3.round((p.ttfbSum / p.count), 2) : 0;
            p.downloadTimeSum += v['downloadTime_avg'] * v['count'];
            p.downloadTimeAvg = p.count ? d3.round((p.downloadTimeSum / p.count), 2) : 0;
            p.sslTimeSum += v['sslTime_avg'] * v['count'];
            p.sslTimeAvg = p.count ? d3.round((p.sslTimeSum / p.count), 2) : 0;
            p.connectTimeSum += v['connectTime_avg'] * v['count'];
            p.connectTimeAvg = p.count ? d3.round((p.connectTimeSum / p.count), 2) : 0;
            return p;
        },
        //remove
        function (p, v) {
            p.count -= v['count'];
            p.loadTimeSum -= v['loadTimeMs_avg'] * v['count'];
            p.loadTimeAvg = p.count ? d3.round((p.loadTimeSum / p.count), 2) : 0;
            p.ttfbSum -= v['ttfb_avg'] * v['count'];
            p.ttfbAvg = p.count ? d3.round((p.ttfbSum / p.count), 2) : 0;
            p.downloadTimeSum -= v['downloadTime_avg'] * v['count'];
            p.downloadTimeAvg = p.count ? d3.round((p.downloadTimeSum / p.count), 2) : 0;
            p.sslTimeSum -= v['sslTime_avg'] * v['count'];
            p.sslTimeAvg = p.count ? d3.round((p.sslTimeSum / p.count), 2) : 0;
            p.connectTimeSum -= v['connectTime_avg'] * v['count'];
            p.connectTimeAvg = p.count ? d3.round((p.connectTimeSum / p.count), 2) : 0;
            return p;
        },
        //init
        function (p, v) {
            return {
                count: 0,
                loadTimeAvg: 0,
                loadTimeSum: 0,
                ttfbAvg: 0,
                ttfbSum: 0,
                downloadTimeAvg: 0,
                downloadTimeSum: 0,
                sslTimeAvg: 0,
                sslTimeSum: 0,
                connectTimeAvg: 0,
                connectTimeSum: 0
            };
        });

    // Accessors for reductio min and max
    var minValueAccessor = function (d) {
        if (d.value.min)
            return d.value.min;
        else
            return null;
    };
    var maxValueAccessor = function (d) {
        if (d.value.max)
            return d.value.max;
        else
            return null;
    };

    var loadTimeGroup_min = reductio().min(function (d) {
        return +d['loadTimeMs_min']
    })(jobId_Date_Dimension.group());
    var loadTimeGroup_max = reductio().max(function (d) {
        return +d['loadTimeMs_max']
    })(jobId_Date_Dimension.group());
    var ttfbGroup_min = reductio().min(function (d) {
        return +d['ttfb_min']
    })(jobId_Date_Dimension.group());
    var ttfbGroup_max = reductio().max(function (d) {
        return +d['ttfb_max']
    })(jobId_Date_Dimension.group());
    var downloadTimeGroup_min = reductio().min(function (d) {
        return +d['downloadTime_min']
    })(jobId_Date_Dimension.group());
    var downloadTimeGroup_max = reductio().max(function (d) {
        return +d['downloadTime_max']
    })(jobId_Date_Dimension.group());
    var sslTimeGroup_min = reductio().min(function (d) {
        return +d['sslTime_min']
    })(jobId_Date_Dimension.group());
    var sslTimeGroup_max = reductio().max(function (d) {
        return +d['sslTime_max']
    })(jobId_Date_Dimension.group());
    var connectTimeGroup_min = reductio().min(function (d) {
        return +d['connectTime_min']
    })(jobId_Date_Dimension.group());
    var connectTimeGroup_max = reductio().max(function (d) {
        return +d['connectTime_max']
    })(jobId_Date_Dimension.group());

    var composite = board.getCompositeChart('dcChart', 'line-chart');

    var allGraphsByJobId = {};

    var colorScale = d3.scale.category20c();
    
    function createLineChart( jobFilter, labelPostfix, name, valueAccessor) {
        var group = remove_empty_bins(filterJobGroup(jobFilter, currentJobId), valueAccessor);
        var label = (labels['job'] ? labels['job'][currentJobId] : currentJobId) + labelPostfix;
        allGraphsByJobId[currentJobId][name] = board.createLineChart(composite, jobId_Date_Dimension, group, colorScale(label), label, valueAccessor);
    }

    for (var j = 0; j < jobs.length; j++) {
        var currentJobId = jobs[j];
        allGraphsByJobId[currentJobId] = {};

        // avg graph loadTime
       createLineChart(loadTime_ttfb_avg, " | LoadTimeMs Avg", "loadTimeAvg", function (d) {
            return d.value.loadTimeAvg;
        });

        // avg graph ttfb
        createLineChart(loadTime_ttfb_avg, " | TTFB Avg","ttfbAvg", function (d) {
            return d.value.ttfbAvg;
        });

        // avg graph downloadTime
        createLineChart(loadTime_ttfb_avg, " | Download Time Avg", "downloadTimeAvg", function (d) {
            return d.value.downloadTimeAvg;
        });

        // avg graph sslTime
        createLineChart(loadTime_ttfb_avg, " | SSL Time Avg", "sslTimeAvg", function (d) {
            return d.value.sslTimeAvg;
        });
        // avg graph sslTime
        createLineChart(loadTime_ttfb_avg, " | Connect Time Avg", "connectTimeAvg", function (d) {
            return d.value.connectTimeAvg;
        });

        // min graph loadTime
        createLineChart(loadTimeGroup_min, " | LoadTimeMs Min", "loadTimeMin", minValueAccessor);
        // min graph ttfb
        createLineChart(ttfbGroup_min, " | TTFB Min", "ttfbMin", minValueAccessor);
        // min graph downloadTime
        createLineChart(downloadTimeGroup_min, " | Download Time Min", "downloadTimeMin", minValueAccessor);
        // min graph sslTime
        createLineChart(sslTimeGroup_min, " | SSL Time Min", "sslTimeMin", minValueAccessor);
        // min graph connectTime
        createLineChart(connectTimeGroup_min, " | Connect Time Min", "connectTimeMin", minValueAccessor);

        // max graph loadTime
        createLineChart(loadTimeGroup_max, " | LoadTimeMs Max", "loadTimeMax", maxValueAccessor);
        // max graph ttfb
        createLineChart(ttfbGroup_max, " | TTFB Max", "ttfbMax", maxValueAccessor);
        // max graph ttfb
        createLineChart(downloadTimeGroup_max, " | Download Time Max", "downloadTimeMax", maxValueAccessor);
        // max graph ttfb
        createLineChart(sslTimeGroup_max, " | SSL Time Max", "sslTimeMax", maxValueAccessor);
        // max graph ttfb
        createLineChart(connectTimeGroup_max, " | Connect Time Max", "connectTimeMax", maxValueAccessor);
    }

    // jobs.length * 6 = [loadTime, ttfb]*[avg,min,max]*[jobId]
    board.addCompositeChart('dcChart', 'line-chart', from, to, jobs.length * 6);
    redrawCompositeChart();

    // ### END LINE CHARTS

    // Add data to dashboard
    var p1 = new Promise(
        function (resolve, reject) {
            board.addData(data);
        }
    );

    // Add onClick Listener
    $(document).on('change', 'input:checkbox[name="measurementCheckbox"]', function (event) {
        redrawCompositeChart();
    });

    function redrawCompositeChart() {
        board.setAnimationTime(500);

        var showAvg = document.getElementById("avg").checked;
        var showMax = document.getElementById("max").checked;
        var showMin = document.getElementById("min").checked;
        var visibleGraphs = [];

        function handleValueTypeCheckbox(avgName,minName,maxName) {
            if (showAvg)
                visibleGraphs.push(jobGraphs[avgName]);
            if (showMin)
                visibleGraphs.push(jobGraphs[minName]);
            if (showMax)
                visibleGraphs.push(jobGraphs[maxName]);
        }
        
        for (var j = 0; j < jobs.length; j++) {
            var jobGraphs = allGraphsByJobId[jobs[j]];
            if (document.getElementById("loadTimeMs").checked) {
                handleValueTypeCheckbox("loadTimeAvg", "loadTimeMin", "loadTimeMax");
            }
            if (document.getElementById("ttfb").checked) {
                handleValueTypeCheckbox("ttfbAvg","ttfbMin", "ttfbMax");
            }
            if (document.getElementById("downloadTime").checked) {
                handleValueTypeCheckbox("downloadTimeAvg","downloadTimeMin", "downloadTimeMax");
            }
            if (document.getElementById("sslTime").checked) {
                handleValueTypeCheckbox("sslTimeAvg","sslTimeMin", "sslTimeMax");
            }
            if (document.getElementById("connectTime").checked) {
                handleValueTypeCheckbox("connectTimeAvg","connectTimeMin", "connectTimeMax");
            }
        }



        board.getCompositeChart('dcChart', 'line-chart').compose(visibleGraphs);
        dc.renderAll();

        board.setAnimationTime(700);
    }
}

/**
 * For values with only one item there is no need for a hole dimension.
 * Instead there is a message shown.
 * @param dataCounts
 * @param data
 * @param labels
 */
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

/**
 * Counts the quantity of the different types.
 * @param data
 * @returns {{}}
 */
function getDataCounts(data) {
    var result = {};

    var uniqueMap = {'browser': [], 'mediaType': [], 'subtype': [], 'host': [], 'page': [], 'measuredEvent': [], 'jobGroup': []};
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
        if (uniqueMap['jobGroup'].indexOf(datum['jobGroup']) < 0) {
            uniqueMap['jobGroup'].push(datum['jobGroup'])
        }
    }

    result['browser'] = uniqueMap['browser'].length;
    result['mediaType'] = uniqueMap['mediaType'].length;
    result['subtype'] = uniqueMap['subtype'].length;
    result['host'] = uniqueMap['host'].length;
    result['page'] = uniqueMap['page'].length;
    result['measuredEvent'] = uniqueMap['measuredEvent'].length;
    result['jobGroup'] = uniqueMap['jobGroup'].length;

    return result;
}

/**
 * Returns a list of unique jobs
 * @param data
 * @returns {Array}
 */
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

/**
 * A fake group which contains only data for the given job group
 * @see https://github.com/dc-js/dc.js/wiki/FAQ#fake-groups
 * @param source_group
 * @param jobId
 * @returns {{all: all, top: top}}
 */
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

/**
 * Creates a new fake group which contains only existing data points
 * @see https://github.com/dc-js/dc.js/wiki/FAQ#fake-groups
 * @param source_group
 * @returns {{all: all}}
 */
function remove_empty_bins(source_group, valueAccessor) {
    return {
        all: function () {
            return source_group.all().filter(function (d) {
                return valueAccessor(d) !== null && valueAccessor(d) != 0;
            });
        }
    };
}