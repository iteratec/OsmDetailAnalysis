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
    %{--<h3 align="center">Detail Dashboard</h3>--}%
    %{--<h4 align="center">${new java.text.SimpleDateFormat().format(fromDate)} - ${new java.text.SimpleDateFormat().format(toDate)}</h4>--}%
    <g:if test="${graphData != "[]"}">
        <div class="">
        <div class="detailDashboardContainer card-well">
            <div class="row" >
                <div class="col-md-8">
                    <div class="card">
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

                    <div class="card" align="left">
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
                    <div class="card">
                        <div class="dc-chartContainer" id="line-chart"></div>
                            <div id="dc-data-count"> </div>
                    </div>
                    <div class="card">
                        <div class="rowChartContainer">
                            <div class="dc-chartContainer" id="timeChart"></div>
                        </div>
                    </div>


                </div>

                <div class="col-md-4 dcControls" >
                    <div class="card" id="dcControlsCard">
                        <div class="section row">
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
                            <div class="rowChartContainer" id="hostChartContainer">
                                <div id="host-chart"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>




        <div id="loadingIndicatorTable" style="display: none;" >
            <i  class="fa  fa-gear fa-spin  loadingIndicatorTable " style=" font-size:60px"></i>
            <div  class="rect placeholder"></div>
        </div>

        <div class="card-modal">
            <div class="card-modal-inner">
                <div class="card" id="assetDetailsContainer">
                    <div class="card-header">
                        <button type="button" class="close" aria-hidden="true">×</button>
                        <h3>Asset Details</h3>
                    </div>
                    <table class="table table-hover" cellspacing="0" width="100%" id="assetDetailsTable">
                        <thead></thead>
                        <tbody></tbody>
                    </table>
                    <label for="wptUrl">WptUrl:</label>
                    <a id="wptUrl"></a>
                </div>
                <div class="card" id="preselectedValuesContainer" style="display: none;">
                    <div class="card-header">
                        <h3>Preselected Values</h3>
                    </div>
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
            </div>
        </div>
        </div>
    </g:if>
    <g:else>
        <div>
            There was no data available for the selected timeframe and filter.
        </div>
    </g:else>
</div>
</div>
