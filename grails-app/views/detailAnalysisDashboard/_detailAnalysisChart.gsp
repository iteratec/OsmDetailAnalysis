<div id="dcChart">
    %{--Needed for OSM integration--}%
    <link rel="stylesheet" type="text/css" href="${serverBaseUrl}/assets/dashboard/DcDashboard.css">
    <i id = "loadingIndicator" class="fa  fa-gear fa-spin dc-chart" style="font-size:60px"></i>
    <div id = "detailDataContainer" style="display: none;">
        <div>
            <h3>Detail Dashboard</h3>
            <g:if test="${graphData != "[]"}">
            <div class="tabbable">
                <ul class="nav nav-tabs">
                    <li class="active"><a href="#detailTab1" data-toggle="tab">General</a></li>
                    <li><a href="#detailTab3" data-toggle="tab">Measured Event</a></li>
                    <li><a href="#detailTab2" data-toggle="tab">Host</a></li>
                </ul>

                <div class="row detailDashboardContainer">
                    <div class="tab-content" style="height: 460px;">
                        <div class="tab-pane active" id="detailTab1">
                            <div class="row" align="center">
                                <div class="span2" style="margin:0 30px">
                                    <div>
                                        <span><strong>By MediaType</strong></span>
                                    </div>

                                    <div class="pieChartDiv" id="mediaType-chart"></div>
                                </div>

                                <div class="span2" style="margin:0 30px">
                                    <div>
                                        <span><strong>By Subtype</strong></span>
                                    </div>

                                    <div class="pieChartDiv" id="subtype-chart"></div>
                                </div>

                                <div class="span2" style="margin:0 30px">
                                    <div>
                                        <span><strong>By Browser</strong></span>
                                    </div>

                                    <div class="pieChartDiv" id="browser-chart"></div>
                                </div>

                                <div class="span2" style="margin:0 30px">
                                    <div>
                                        <span><strong>By Page</strong></span>
                                    </div>

                                    <div class="pieChartDiv" id="page-chart"></div>

                                </div>
                            </div>
                             <div class="row" align="center">
                                 <div class="span2" style="margin:0 30px">
                                     <div>
                                         <span><strong>By Job Group</strong></span>
                                     </div>

                                     <div class="pieChartDiv" id="jobGroup-chart"></div>

                                 </div>
                             </div>
                        </div>

                        <div class="tab-pane" id="detailTab3" style="overflow-x: hidden">
                            <div class="row">
                                <div class="span12">
                                    <div id="measuredEvent-chart"></div>
                                </div>
                            </div>
                        </div>

                        <div class="tab-pane" id="detailTab2" style="overflow-x: hidden">
                            <div class="row">
                                <div class="span12">
                                    <div id="host-chart"></div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
            </g:if>
            <g:else>
                There was no data available for the selected timeframe and filter.
            </g:else>
        </div>
        <g:if test="${graphData != "[]"}">
        <div>
            <div class="row dashboardContainer">
                <div class="row">
                    <div class="span12">
                        <div id="dc-data-count"/>
                    </div>
                </div>
            </div>
        </div>
        <br>

        <div>
            <div>
                <div class="row dashboardContainer">

                    <div class="row">
                        <div class="span12">
                            <div id="timeChart"></div>
                        </div>
                    </div>
                    <div>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="loadTimeMs" checked="checked">
                            Load Time
                        </label>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="ttfb">
                            Time to first byte
                        </label>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="downloadTime">
                            Download Time
                        </label>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="sslTime">
                            SSL Negotiation Time
                        </label>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="connectTime">
                            Connect Time
                        </label>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="dnsTime">
                            DNS Time
                        </label>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="bytesIn">
                            Bytes In
                        </label>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="bytesOut">
                            Bytes Out
                        </label>
                    </div>

                    <div>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="avg" checked="checked">
                            Avg
                        </label>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="min">
                            Min
                        </label>
                        <label class="checkbox inline">
                            <input type="checkbox" name="measurementCheckbox" id="max">
                            Max
                        </label>
                    </div>

                    <div class="row" style="min-height: 700px; overflow-y: visible">
                        <div class="span12">
                            <div id="line-chart"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

 <br>


    <div class="container" id = "assetDetailsContainer" style="display: none;">

        <table   class="table table-hover" cellspacing="0" width="100%" id="assetDetailsTable" >
            <thead></thead>
            <tbody></tbody>
        </table>
        <br>
        <h3>Preselected Values</h3>
        <table   class="table table-hover" cellspacing="0" width="100%" id="preFilterTable" >
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
</div>
