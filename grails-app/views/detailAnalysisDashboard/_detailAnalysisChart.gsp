<div id="dcChart">
    %{--Needed for OSM integration--}%
    <link rel="stylesheet" type="text/css" href="${serverBaseUrl}/assets/dashboard/DcDashboard.css">
    <g:if test="${graphData != "[]"}">
        <div id="loadingIndicatorGlobal">
            <i class="fa  fa-gear fa-spin global loadingIndicator " style="font-size:60px"></i>

            <div class="rect placeholder"></div>
        </div>
    </g:if>
    <g:if test="${graphData != "[]"}">
        <div id="detailDataContainer" class="container-fluid" style="display: none;">
            <div class="detailDashboardContainer card-well">
                <div class="row">
                    <div class="col-md-8">
                        <div class="card">
                            <div class="btn-group" data-toggle="buttons">
                                <label class="btn btn-primary active">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="loadTimeMs" checked>
                                    Load Time
                                </label>
                                <label class="btn btn-default ">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="ttfb">
                                    Time to first byte
                                </label>
                                <label class="btn btn-default ">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="downloadTime">
                                    Download Time
                                </label>
                                <label class="btn btn-default ">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="sslTime">
                                    SSL Negotiation Time
                                </label>
                                <label class="btn btn-default ">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="connectTime">
                                    Connect Time
                                </label>
                                <label class="btn btn-default ">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="dnsTime">
                                    DNS Time
                                </label>
                                <label class="btn btn-default ">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="bytesIn">
                                    Bytes In
                                </label>
                                <label class="btn btn-default ">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="bytesOut">
                                    Bytes Out
                                </label>
                            </div>

                            <div id="checkboxAvgMinMaxContainer" class="btn-group" data-toggle="buttons">
                                <label class="btn btn-primary active">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="avg" checked> Avg
                                </label>
                                <label class="btn btn-default">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="min"> Min
                                </label>
                                <label class="btn btn-default">
                                    <input type="checkbox" name="measurementCheckbox" autocomplete="off" id="max"> Max
                                </label>
                            </div>
                        </div>

                        <div class="card">
                            <div class="dc-chartContainer" id="line-chart"></div>

                            <div id="dc-data-count"></div>
                        </div>

                        <div class="card">
                            <div class="rowChartContainer">
                                <div class="dc-chartContainer" id="timeChart"></div>
                            </div>

                            <div id="timeChartExplanation"><g:message code="de.iteratec.osm.da.timechart.explanation"/></div>
                        </div>
                    </div>

                    <div class="col-md-4 dcControls">
                        <div class="card" id="dcControlsCard">
                            <div class="section row">
                                <div class="col-md-4">
                                    <div class="row">
                                        <div>
                                            <span><strong><g:message code="de.iteratec.osm.da.piechart.title.jobgroup"/></strong></span>
                                        </div>

                                        <div class="pieChartDiv" id="jobGroup-chart"></div>
                                    </div>

                                    <div class="row">
                                        <div>
                                            <span><strong><g:message code="de.iteratec.osm.da.piechart.title.browser"/></strong></span>
                                        </div>

                                        <div class="pieChartDiv" id="browser-chart"></div>
                                    </div>
                                </div>

                                <div class="col-md-4">
                                    <div class="row">
                                        <div>
                                            <span><strong><g:message code="de.iteratec.osm.da.piechart.title.page"/></strong></span>
                                        </div>

                                        <div class="pieChartDiv" id="page-chart"></div>
                                    </div>

                                    <div class="row">
                                        <div>
                                            <span><strong><g:message code="de.iteratec.osm.da.piechart.title.measuredevent"/></strong></span>
                                        </div>

                                        <div class="pieChartDiv" id="measuredEvent-chart"></div>
                                    </div>
                                </div>

                                <div class="col-md-4">
                                    <div class="row">
                                        <div>
                                            <span><strong><g:message code="de.iteratec.osm.da.piechart.title.mediatype"/></strong></span>
                                        </div>

                                        <div class="pieChartDiv" id="mediaType-chart"></div>
                                    </div>

                                    <div class="row">
                                        <div>
                                            <span><strong><g:message code="de.iteratec.osm.da.piechart.title.subtype"/></strong></span>
                                        </div>

                                        <div class="pieChartDiv" id="subtype-chart"></div>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="rowChartContainer" id="hostChartContainer">
                                    <div id="host-chart"></div>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>

            </div>

            <div id="loadingIndicatorTable" style="display: none;">
                <i class="fa  fa-gear fa-spin  loadingIndicatorTable " style=" font-size:60px"></i>

                <div class="rect placeholder"></div>
            </div>

            <div class="card-modal">
                <div class="card-modal-inner">
                    <div class="card" id="assetDetailsContainer">
                        <div class="card-header centered">
                            <button type="button" class="close" aria-hidden="true">×</button>

                            <h3 id="assetDetailsHeadline">Asset Details</h3>
                            <label for="wptUrl">WptUrl:</label>
                            <a id="wptUrl"></a>
                        </div>

                        <div id="assetDetailsDatatableContainer" style="display: none;">
                            <table class="table table-hover" cellspacing="0" width="100%" id="assetDetailsTable">
                                <thead></thead>
                                <tbody></tbody>
                            </table>
                            <hr id="assetTablesSeperator">
                        </div>

                        <div class="card-header" id="preselectedValuesHeader">
                            <h3><g:message code="de.iteratec.osm.da.preselectedValues.headline"/></h3>
                        </div>
                        <table class="table table-hover" cellspacing="0" width="100%" id="preFilterTable">
                            <thead>
                            <tr>
                                <th><g:message code="de.iteratec.osm.da.preselectedValues.property"/></th>
                                <th><g:message code="de.iteratec.osm.da.preselectedValues.value"/></th>
                            </tr>
                            </thead>
                            <tbody></tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </g:if>
    <g:else>
        <div class="card-well">
            <div class="row">
                <div class="col-md-12">
                    <div class="card" id="noDataInfo">
                        <g:message code="de.iteratec.osm.da.nodata.info"/>
                    </div>
                </div>
            </div>
        </div>
    </g:else>
</div>
