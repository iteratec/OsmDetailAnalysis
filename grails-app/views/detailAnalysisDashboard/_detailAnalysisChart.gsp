<div id="dcChart">
    %{--Needed for OSM integration--}%
    <link rel="stylesheet" type="text/css" href="${serverBaseUrl}/assets/dashboard/DcDashboard.css">
    <g:if test="${graphData != "[]"}">
        <div id="loadingIndicatorGlobal">
            <i  class="fa  fa-gear fa-spin global loadingIndicator " style="font-size:60px"></i>
            <div  class="rect placeholder"></div>
        </div>
    </g:if>
<div id="detailDataContainer" class="container-fluid" style="display: none;">
    <h3 align="center">Detail Dashboard</h3>
    <h4 align="center">${new java.text.SimpleDateFormat().format(fromDate)} - ${new java.text.SimpleDateFormat().format(toDate)}</h4>
    <g:if test="${graphData != "[]"}">
        <div class="detailDashboardContainer">
            <div class="row">
                <div class="col-md-8">
                    <div>
                        <label class="checkbox-inline" >
                            <input type="checkbox" name="measurementCheckbox" id="loadTimeMs" checked="checked">
                            Load Time
                        </label>
                        <label class="checkbox-inline">
                            <input type="checkbox" name="measurementCheckbox" id="ttfb">
                            Time to first byte
                        </label>
                        <label class="checkbox-inline">
                            <input type="checkbox" name="measurementCheckbox" id="downloadTime">
                            Download Time
                        </label>
                        <label class="checkbox-inline">
                            <input type="checkbox" name="measurementCheckbox" id="sslTime">
                            SSL Negotiation Time
                        </label>
                        <label class="checkbox-inline">
                            <input type="checkbox" name="measurementCheckbox" id="connectTime">
                            Connect Time
                        </label>
                        <label class="checkbox-inline">
                            <input type="checkbox" name="measurementCheckbox" id="dnsTime">
                            DNS Time
                        </label>
                        <label class="checkbox-inline">
                            <input type="checkbox" name="measurementCheckbox" id="bytesIn">
                            Bytes In
                        </label>
                        <label class="checkbox-inline">
                            <input type="checkbox" name="measurementCheckbox" id="bytesOut">
                            Bytes Out
                        </label>
                    </div>

                    <div align="left">
                        <label class="checkbox-inline">
                            <input type="checkbox" name="measurementCheckbox" id="avg" checked="checked">
                            Avg
                        </label>
                        <label class="checkbox-inline">
                            <input type="checkbox" name="measurementCheckbox" id="min">
                            Min
                        </label>
                        <label class="checkbox-inline">
                            <input type="checkbox" name="measurementCheckbox" id="max">
                            Max
                        </label>
                    </div>

                    <div class="dc-chartContainer" id="line-chart"></div>
                    <div class="row">
                        <div class="col-md-12">
                            <div id="dc-data-count"> </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-12">
                            <div class="rowChartContainer">
                                <div class="dc-chartContainer" id="timeChart"></div>
                            </div>
                        </div>
                    </div>


                </div>

                <div class="col-md-4 dcControls">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="row">
                                <div class="col-md-12">
                                    <div>
                                        <span><strong>By MediaType</strong></span>
                                    </div>

                                    <div class="pieChartDiv" id="mediaType-chart"></div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div>
                                        <span><strong>By Subtype</strong></span>
                                    </div>

                                    <div class="pieChartDiv" id="subtype-chart"></div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div>
                                        <span><strong>By Browser</strong></span>
                                    </div>

                                    <div class="pieChartDiv" id="browser-chart"></div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="row">
                                <div class="col-md-12">
                                    <div>
                                        <span><strong>By Page</strong></span>
                                    </div>

                                    <div class="pieChartDiv" id="page-chart"></div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div>
                                        <span><strong>By Job Group</strong></span>
                                    </div>

                                    <div class="pieChartDiv" id="jobGroup-chart"></div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-12">
                                    <div>
                                        <span><strong>By Measured Event</strong></span>
                                    </div>

                                    <div class="pieChartDiv" id="measuredEvent-chart"></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="rowChartContainer">
                            <div id="host-chart"></div>
                        </div>
                    </div>
                </div>
            </div>

        </div>




        <br>
        <div id="loadingIndicatorTable" style="display: none;" >
            <i  class="fa  fa-gear fa-spin  loadingIndicatorTable " style=" font-size:60px"></i>
            <div  class="rect placeholder"></div>
        </div>

        <div class="container-fluid" id="assetDetailsContainer" style="display: none;">

            <table class="table table-hover" cellspacing="0" width="100%" id="assetDetailsTable">
                <thead></thead>
                <tbody></tbody>
            </table>
            <label for="wptUrl">WptUrl:</label>
            <div id="wptUrl">DuSiehstMichNicht</div>
        </div>

        <div class="container-fluid" id="preselectedValuesContainer" style="display: none;">
            <h3>Preselected Values</h3>
            <table class="table table-hover" cellspacing="0" width="100%" id="preFilterTable">
                <thead>
                <tr>
                    <th>Property</th>
                    <th>Value</th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </g:if>
    <g:else>
        <div>
            There was no data available for the selected timeframe and filter.
        </div>
    </g:else>
</div>
</div>
