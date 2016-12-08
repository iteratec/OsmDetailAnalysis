//= require_tree ../bower_components/datatables.net
//= require_tree ../bower_components/datatables.net-dt
//= require_tree ../bower_components/crossfilter2
//= require_tree ../bower_components/d3
//= require_tree ../bower_components/dcjs
//= require_tree ../bower_components/reductio
//= require DcDashboard.js

function createDashboard(data, labelsParam, from, to, ajaxUrlParam) {
    labels = labelsParam;
    ajaxUrl = ajaxUrlParam;
    createChangeSelectionButton();
    if (data[0] == undefined) {
        //No data to show, so just stop here
        $("detailDataContainer").show();
        return;
    }
    board = new DcDashboard();
    // Set dashboard width same as div width
    // var width = $(".dashboardContainer").css("width").replace("px", "");
    // board.setDashboardWidth(+width);
    charts = new Object();
    var dataCounts = getDataCounts(data);
    showUniqueValues(dataCounts, data, labels);

    // BROWSER FILTER
    // if (dataCounts['browser'] > 1) {
        var browser = board.allData.dimension(function (d) {
            return "" + d['browser'];
        });
        var browserGroup = browser.group().reduceSum(function (d) {
            return d['count']
        });
        var browserLabelAccessor = function (d) {
            return labels['browser'] ? labels['browser'][d.key] : d.key
        };
        charts["browser-chart"] = board.addPieChart('dcChart', 'browser-chart', browser, browserGroup, browserLabelAccessor);
    // }

    // MEDIATYPE FILTER
    // if (dataCounts['mediaType'] > 1) {
        var mediaType = board.allData.dimension(function (d) {
            return "" + d['mediaType'];
        });
        var mediaTypeGroup = mediaType.group().reduceSum(function (d) {
            return d['count']
        });
        var mediaTypeLabelAccessor = function (d) {
            return d.key
        };
        charts["mediaType-chart"] = board.addPieChart('dcChart', 'mediaType-chart', mediaType, mediaTypeGroup, mediaTypeLabelAccessor);
    // }

    // SUBTYPE FILTER
    // if (dataCounts['subtype'] > 1) {
        var subtype = board.allData.dimension(function (d) {
            return "" + d['subtype'];
        });
        var subtypeGroup = subtype.group().reduceSum(function (d) {
            return d['count'];
        });
        var subtypeLabelAccessor = function (d) {
            return d.key
        };
        charts["subtype-chart"] = board.addPieChart('dcChart', 'subtype-chart', subtype, subtypeGroup, subtypeLabelAccessor);
    // }

    // PAGE FILTER
    // if (dataCounts['page'] > 1) {
        var page = board.allData.dimension(function (d) {
            return "" + d['page'];
        });
        var pageGroup = page.group().reduceSum(function (d) {
            return d['count'];
        });
        var pageLabelAccessor = function (d) {
            return labels['page'] ? labels['page'][d.key] : d.key
        };
        charts["page-chart"] = board.addPieChart('dcChart', 'page-chart', page, pageGroup, pageLabelAccessor);
    // }

    //JOBGROUP FILTER
    // if (dataCounts['jobGroup'] > 1) {
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
        charts["jobGroup-chart"] = board.addPieChart('dcChart', 'jobGroup-chart', jobGroup, jobGroupGroup, jobGroupLabelAccessor)
    // }

    // MEASURED EVENT FILTER
    // if (dataCounts['measuredEvent'] > 1) {
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
        charts["measuredEvent-chart"] = board.addPieChart('dcChart', 'measuredEvent-chart', measuredEvent, measuredEventGroup,  measuredEventLabelAccessor);
        // charts["measuredEvent-chart"] = board.addRowChart('dcChart', 'measuredEvent-chart', measuredEvent, measuredEventGroup, dataCounts['measuredEvent'], measuredEventLabelAccessor);
    // }

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
        charts["host-chart"] = board.addRowChart('dcChart', 'host-chart', host, hostGroup, dataCounts['host'], hostLabelAccessor);
    }

    var loadingIndicatorGlobal = document.getElementById("loadingIndicatorGlobal");
    loadingIndicatorGlobal.style.display = 'none';
    var detailDataContainer = document.getElementById("detailDataContainer");
    detailDataContainer.style.display = 'block';

    // Show count of selected data
    board.addDataCount('dcChart', 'dc-data-count');

    // ### BEGIN LINE CHARTS
    var date_Dimension = board.allData.dimension(function (d) {
        return d.date;
    });

    var loadTime_ttfb_avg = date_Dimension.group().reduce(
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
            p.dnsTimeSum += v['dnsTime_avg'] * v['count'];
            p.dnsTimeAvg = p.count ? d3.round((p.dnsTimeSum / p.count), 2) : 0;
            p.bytesInSum += v['bytesIn_avg'] * v['count'];
            p.bytesInAvg = p.count ? d3.round((p.bytesInSum / p.count), 2) : 0;
            p.bytesOutSum += v['bytesOut_avg'] * v['count'];
            p.bytesOutAvg = p.count ? d3.round((p.bytesOutSum / p.count), 2) : 0;
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
            p.dnsTimeSum -= v['dnsTime_avg'] * v['count'];
            p.dnsTimeAvg = p.count ? d3.round((p.dnsTimeSum / p.count), 2) : 0;
            p.bytesInSum -= v['bytesIn_avg'] * v['count'];
            p.bytesInAvg = p.count ? d3.round((p.bytesInSum / p.count), 2) : 0;
            p.bytesOutSum -= v['bytesOut_avg'] * v['count'];
            p.bytesOutAvg = p.count ? d3.round((p.bytesOutSum / p.count), 2) : 0;
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
                connectTimeSum: 0,
                dnsTimeAvg: 0,
                dnsTimeSum: 0,
                bytesInAvg: 0,
                bytesInSum: 0,
                bytesOutAvg: 0,
                bytesOutSum: 0
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
    })(date_Dimension.group());
    var loadTimeGroup_max = reductio().max(function (d) {
        return +d['loadTimeMs_max']
    })(date_Dimension.group());
    var ttfbGroup_min = reductio().min(function (d) {
        return +d['ttfb_min']
    })(date_Dimension.group());
    var ttfbGroup_max = reductio().max(function (d) {
        return +d['ttfb_max']
    })(date_Dimension.group());
    var downloadTimeGroup_min = reductio().min(function (d) {
        return +d['downloadTime_min']
    })(date_Dimension.group());
    var downloadTimeGroup_max = reductio().max(function (d) {
        return +d['downloadTime_max']
    })(date_Dimension.group());
    var sslTimeGroup_min = reductio().min(function (d) {
        return +d['sslTime_min']
    })(date_Dimension.group());
    var sslTimeGroup_max = reductio().max(function (d) {
        return +d['sslTime_max']
    })(date_Dimension.group());
    var connectTimeGroup_min = reductio().min(function (d) {
        return +d['connectTime_min']
    })(date_Dimension.group());
    var connectTimeGroup_max = reductio().max(function (d) {
        return +d['connectTime_max']
    })(date_Dimension.group());
    var dnsTimeGroup_min = reductio().min(function (d) {
        return +d['dnsTime_min']
    })(date_Dimension.group());
    var dnsTimeGroup_max = reductio().max(function (d) {
        return +d['dnsTime_max']
    })(date_Dimension.group());
    var bytesInGroup_min = reductio().min(function (d) {
        return +d['bytesIn_min']
    })(date_Dimension.group());
    var bytesInGroup_max = reductio().max(function (d) {
        return +d['bytesIn_max']
    })(date_Dimension.group());
    var bytesOutGroup_min = reductio().min(function (d) {
        return +d['bytesOut_min']
    })(date_Dimension.group());
    var bytesOutGroup_max = reductio().max(function (d) {
        return +d['bytesOut_max']
    })(date_Dimension.group());

    var composite = board.getCompositeChart('dcChart', 'line-chart');

    var allGraphs = {};

    var colorScale = d3.scale.category10();

    function createLineChart(jobFilter, label, name, valueAccessor, unit) {
        var group = remove_empty_bins(jobFilter, valueAccessor);
        allGraphs[name] = board.createLineChart(composite, date_Dimension, group, colorScale(label), label, valueAccessor, unit);
    }

    // avg graph loadTime
    createLineChart(loadTime_ttfb_avg, "LoadTimeMs Avg", "loadTimeAvg", function (d) {
        return d.value.loadTimeAvg;
    });

    // avg graph ttfb
    createLineChart(loadTime_ttfb_avg, "TTFB Avg", "ttfbAvg", function (d) {
        return d.value.ttfbAvg;
    });

    // avg graph downloadTime
    createLineChart(loadTime_ttfb_avg, "Download Time Avg", "downloadTimeAvg", function (d) {
        return d.value.downloadTimeAvg;
    });

    // avg graph sslTime
    createLineChart(loadTime_ttfb_avg, "SSL Time Avg", "sslTimeAvg", function (d) {
        return d.value.sslTimeAvg;
    });
    // avg graph connectTime
    createLineChart(loadTime_ttfb_avg, "Connect Time Avg", "connectTimeAvg", function (d) {
        return d.value.connectTimeAvg;
    });
    // avg graph dnsTime
    createLineChart(loadTime_ttfb_avg, "DNS Time Avg", "dnsTimeAvg", function (d) {
        return d.value.dnsTimeAvg;
    });
    // avg graph bytesIn
    createLineChart(loadTime_ttfb_avg, "Bytes In Avg", "bytesInAvg", function (d) {
        return d.value.bytesInAvg;
    }, "bytes");
    // avg graph bytesOut
    createLineChart(loadTime_ttfb_avg, "Bytes Out Avg", "bytesOutAvg", function (d) {
        return d.value.bytesOutAvg;
    }, "bytes");

    // min graph loadTime
    createLineChart(loadTimeGroup_min, "LoadTimeMs Min", "loadTimeMin", minValueAccessor);
    // min graph ttfb
    createLineChart(ttfbGroup_min, "TTFB Min", "ttfbMin", minValueAccessor);
    // min graph downloadTime
    createLineChart(downloadTimeGroup_min, "Download Time Min", "downloadTimeMin", minValueAccessor);
    // min graph sslTime
    createLineChart(sslTimeGroup_min, "SSL Time Min", "sslTimeMin", minValueAccessor);
    // min graph connectTime
    createLineChart(connectTimeGroup_min, "Connect Time Min", "connectTimeMin", minValueAccessor);
    // min graph dnsTime
    createLineChart(dnsTimeGroup_min, "DNS Time Min", "dnsTimeMin", minValueAccessor);
    // min graph bytesIn
    createLineChart(bytesInGroup_min, "Bytes In Min", "bytesInMin", minValueAccessor, "bytes");
    // min graph bytesOut
    createLineChart(bytesInGroup_min, "Bytes Out Min", "bytesOutMin", minValueAccessor, "bytes");

    // max graph loadTime
    createLineChart(loadTimeGroup_max, "LoadTimeMs Max", "loadTimeMax", maxValueAccessor);
    // max graph ttfb
    createLineChart(ttfbGroup_max, "TTFB Max", "ttfbMax", maxValueAccessor);
    // max graph ttfb
    createLineChart(downloadTimeGroup_max, "Download Time Max", "downloadTimeMax", maxValueAccessor);
    // max graph ttfb
    createLineChart(sslTimeGroup_max, "SSL Time Max", "sslTimeMax", maxValueAccessor);
    // max graph ttfb
    createLineChart(connectTimeGroup_max, "Connect Time Max", "connectTimeMax", maxValueAccessor);
    // max graph dns
    createLineChart(dnsTimeGroup_max, "DNS Time Max", "dnsTimeMax", maxValueAccessor);
    // max graph bytesIn
    createLineChart(bytesInGroup_max, "Bytes In Max", "bytesInMax", maxValueAccessor, "bytes");
    // max graph bytesOut
    createLineChart(bytesOutGroup_max, "Bytes Out Max", "bytesOutMax", maxValueAccessor, "bytes");

    // 24 = [loadTime, ttfb, ..., bytesIn]*[avg,min,max]
    board.addCompositeChart('dcChart', 'line-chart', from, to, 24);
    redrawCompositeChart();

    // ### END LINE CHARTS

    // ### BEGIN TIME CHART
    var timeDimension = board.allData.dimension(function (d) {
        return d.date;
    });

    var timeGroup = timeDimension.group().reduceSum(function (d) {
        return d['count'];
    });
    board.addTimeChart('dcChart', 'timeChart', timeDimension, timeGroup, from, to);
    // ### END TIME CHART

    // Add data to dashboard
    var p1 = new Promise(
        function (resolve, reject) {
            board.addData(data);
        }
    );

    // Add onClick Listener
    $(document).on('change', 'input:checkbox[name="measurementCheckbox"]', function (event) {
        $(this).parent().toggleClass( "btn-primary" );
        $(this).parent().toggleClass( "btn-default" );
        redrawCompositeChart();
        $(this).parent().removeClass( "focus" );
    });

    $(document).keyup(function(e) {
        if (e.keyCode == 27) {
            $(".card-modal").hide();
            $("#preselectedValuesContainer").hide();
        }
    });

    $('.card-modal').click(function() {
        $(".card-modal").hide();
        $("#preselectedValuesContainer").hide();
    });
    $('.card-header .close').click(function() {
        $(".card-modal").hide();
        $("#preselectedValuesContainer").hide();
    });

    $('.card-modal-inner').click(function(e) {
        e.stopPropagation();
    });

    function redrawCompositeChart() {
        board.setAnimationTime(500);

        var showAvg = document.getElementById("avg").checked;
        var showMax = document.getElementById("max").checked;
        var showMin = document.getElementById("min").checked;
        var visibleGraphs = [];

        function handleValueTypeCheckbox(avgName, minName, maxName) {
            if (showAvg)
                visibleGraphs.push(allGraphs[avgName]);

            if (showMin)
                visibleGraphs.push(allGraphs[minName]);
            if (showMax)
                visibleGraphs.push(allGraphs[maxName]);
        }

        if (document.getElementById("loadTimeMs").checked) {
            handleValueTypeCheckbox("loadTimeAvg", "loadTimeMin", "loadTimeMax");
        }
        if (document.getElementById("ttfb").checked) {
            handleValueTypeCheckbox("ttfbAvg", "ttfbMin", "ttfbMax");
        }
        if (document.getElementById("downloadTime").checked) {
            handleValueTypeCheckbox("downloadTimeAvg", "downloadTimeMin", "downloadTimeMax");
        }
        if (document.getElementById("sslTime").checked) {
            handleValueTypeCheckbox("sslTimeAvg", "sslTimeMin", "sslTimeMax");
        }
        if (document.getElementById("connectTime").checked) {
            handleValueTypeCheckbox("connectTimeAvg", "connectTimeMin", "connectTimeMax");
        }
        if (document.getElementById("dnsTime").checked) {
            handleValueTypeCheckbox("dnsTimeAvg", "dnsTimeMin", "dnsTimeMax");
        }
        if (document.getElementById("bytesIn").checked) {
            handleValueTypeCheckbox("bytesInAvg", "bytesInMin", "bytesInMax");
        }
        if (document.getElementById("bytesOut").checked) {
            handleValueTypeCheckbox("bytesOutAvg", "bytesOutMin", "bytesOutMax");
        }


        board.getCompositeChart('dcChart', 'line-chart').compose(visibleGraphs);
        dc.renderAll();
        addOnClickListeners();
        hideDataTable();
        board.setAnimationTime(700);
    }
}

function createChangeSelectionButton() {
    var linkText = document.createTextNode("Change Selection");
    var element = document.createElement("a");
    element.appendChild(linkText);
    var currentUrl = window.location.href;
    var newFunction = "eventResultDashboard/showAll";
    var newUrl = currentUrl.replace("detailAnalysis/show", newFunction);
    element.setAttribute("href", newUrl);
    element.setAttribute("class", "btn btn-primary");
    element.setAttribute("value", "ChangeSelection");
    element.setAttribute("name", "ChangeSelection");
    element.style.float = "right";
    element.style.marginRight = "15px";
    var selectionSummary = document.getElementById("selectionSummary");
    selectionSummary.appendChild(element);
}

/**
 * For values with only one item there is no need for a hole dimension.
 * Instead there is a message shown.
 * @param dataCounts
 * @param data
 * @param labels
 */
function showUniqueValues(dataCounts, data, labels) {
    uniqueValues = {};

    for (var key in dataCounts) {
        if (dataCounts[key] <= 1) {
            uniqueValues[key] = data[0][key]
        }
    }

    // if (Object.keys(uniqueValues).length >= 0) {
    //     for (var key in uniqueValues) {
    //         var label = labels[key] ? labels[key][uniqueValues[key]] : uniqueValues[key];
    //         var summary = OsmDetailMicroService.i18n.allValuesEqual;
    //         summary += "<br/>" + key + ": " + label;
    //         document.getElementById(key + "-chart").innerHTML = summary
    //     }
    // }
}

/**
 * Counts the quantity of the different types.
 * @param data
 * @returns {{}}
 */
function getDataCounts(data) {
    var result = {};

    var uniqueMap = {};

    for (var i = 0; i < data.length; i++) {
        var current_entry = data[i];
        for (var key in current_entry) {
            if (typeof  uniqueMap[key] == 'undefined')uniqueMap[key] = {};
            uniqueMap[key][current_entry[key]] = true;

        }
    }
    for (var key in uniqueMap) {
        var keys = Object.keys(uniqueMap[key]);
        result[key] = keys.length

    }

    return result;
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
/**
 * Attaches the onClick-listeners to the individual data points
 */
function addOnClickListeners() {
    d3.selectAll("circle").on("click", function (d) {

        hideDataTable();

        // show loading indicator
        var loadingIndicatorTable = document.getElementById("loadingIndicatorTable");
        loadingIndicatorTable.style.display = 'block';

        // highlight clicked data point
        d3.select(this)
            .style("fill", "red")
            .style("stroke", "black")
            .style("stroke-width", 2)
            .style("opacity", 1);

        var data = {
            date: [],
            host: [],
            browser: [],
            mediaType: [],
            subtype: [],
            jobGroup: [],
            page: []
        };

        if (d.data.key != null) {
            data["date"] = d.data.key;
        }
        if (charts["host-chart"] != null) {
            data["host"] = (charts["host-chart"].filters());
        }
        if (charts["browser-chart"] != null) {
            data["browser"] = (charts["browser-chart"].filters());
        }
        if (charts["mediaType-chart"] != null) {
            data["mediaType"] = (charts["mediaType-chart"].filters());
        }
        if (charts["subtype-chart"] != null) {
            data["subtype"] = (charts["subtype-chart"].filters());
        }
        if (charts["jobGroup-chart"] != null) {
            data["jobGroup"] = (charts["jobGroup-chart"].filters());
        }
        if (charts["page-chart"] != null) {
            data["page"] = (charts["page-chart"].filters());
        }

        jQuery.ajax({
            type: "POST",
            url: ajaxUrl,
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            data: JSON.stringify(data),
            success: function (resp) {
                removeAllRowsFromAssetDetailsTable();
                var uniqueMap = {};
                createWptUrl(resp);
                var dataCount = getDataCounts(resp);
                uniqueMap = createUniqueMapFromDataCount(dataCount, resp);
                fillPreFilteredTable(resp, uniqueMap);
                fillDataInAssetTable(resp, data,uniqueMap);
            }
        });
    });
}

function createWptUrl(resp) {
    var wptUrl = document.getElementById("wptUrl");
    while (wptUrl.firstChild) {
        wptUrl.removeChild(wptUrl.firstChild);
    }

    var urlString = resp[0].wptBaseUrl;
    if (urlString.substr(-1) != '/') urlString += '/';
    urlString += "result/"+ resp[0].wptTestId;

    var linkText = document.createTextNode(labels['measuredEvent'][resp[0].measuredEvent]);
    wptUrl.appendChild(linkText);
    wptUrl.href = urlString;
}
function createUniqueMapFromDataCount(dataCount,data) {
    var uniqueMap = {};
    for (var key in dataCount) {
        if (dataCount[key] <= 1) {
            uniqueMap[key] = data[0][key]
        }
    }
    return uniqueMap;
}
function fillPreFilteredTable(data,uniqueMap) {
    var isEmpty = true;
    var preFilteredTable = document.getElementById("preFilterTable").getElementsByTagName('tbody')[0];
    for (var key in uniqueMap) {
            isEmpty=false;
            var row = preFilteredTable.insertRow(0);
            var cellKey = row.insertCell(0);
            cellKey.innerHTML = key;
            var cellValue = row.insertCell(1);
            cellValue.innerHTML = getLable(key, uniqueMap[key])
    }

}


function getLable(key, value) {
    var result = value;
    if (key in labels && value in labels[key])
        result = labels[key][value];
    return result
}

function removeAllRowsFromAssetDetailsTable() {
    var loadingIndicatorTable = document.getElementById("loadingIndicatorTable");
    loadingIndicatorTable.style.display = 'none';
    if (typeof assetDataTable != 'undefined') {
        assetDataTable.clear();
        assetDataTable.destroy();
    }
    var tableHead = document.getElementById("assetDetailsTable").getElementsByTagName('thead')[0];
    if (tableHead.rows.length > 0) {
        tableHead.deleteRow(0);
    }
    var tableBody = document.getElementById("assetDetailsTable").getElementsByTagName('tbody')[0];
    while (tableBody.rows.length > 0) {
        tableBody.deleteRow(0);
    }
    var preFilterTableBody = document.getElementById("preFilterTable").getElementsByTagName('tbody')[0];
    while (preFilterTableBody.rows.length > 0) {
        preFilterTableBody.deleteRow(0);
    }
}

function fillDataInAssetTable(resp, requestData,uniqueMap) {

    $(".card-modal").show();
    var tableContainer = document.getElementById("assetDetailsContainer");
    tableContainer.style.display = 'block';
    var tableBody = document.getElementById("assetDetailsTable").getElementsByTagName('tbody')[0];
    var tableHead = document.getElementById("assetDetailsTable").getElementsByTagName('thead')[0];
    var columnsMapping = [];

    for (var k in resp[0]) {
        // if (typeof resp[k] == 'undefined') continue;
        if (k == "_id") continue; //filter out mongodb id
        if (k in uniqueMap) continue; // filter out params that are unique in the preselection e.g. if all measurements were done with chrome

        columnsMapping.push(k);
    }
    columnsMapping.sort();
    var headRow = tableHead.insertRow(0);
    columnsMapping.forEach(function (d, i) {
        var cell = headRow.insertCell(i);
        cell.innerHTML = d;
    });
    resp.forEach(function (asset) {
        var row = tableBody.insertRow(0);
        var cells = {};
        var i = 0;
        for (var k in columnsMapping) {
            cells[i] = row.insertCell(i++);
        }
        for (var k in asset) {
            if (columnsMapping.indexOf(k) != -1)
                cells[columnsMapping.indexOf(k)].innerHTML = getLable(k, asset[k])
        }
    });
    if(columnsMapping.length > 0) {
        $("#assetDetailsDatatableContainer").show();
        $("#preselectedValuesHeader").show();
        assetDataTable = $('#assetDetailsTable').DataTable({
            paging: true,
            scrollX:true
        });
        $(".card-modal").show();
    }

}

function hideDataTable() {
    // all data points unhighlighted

    d3.selectAll("circle")
        .style("fill", null)
        .style("stroke", null)
        .style("stroke-width", null)
        .style("opacity", 0.6);
    $("#assetDetailsDatatableContainer").hide();
    $("#preselectedValuesHeader").hide();
}

// Data is set in the detailAnalysis/show.gsp
createDashboard(OsmDetailMicroService.data, OsmDetailMicroService.labels, OsmDetailMicroService.from, OsmDetailMicroService.to, OsmDetailMicroService.ajaxUrl);
