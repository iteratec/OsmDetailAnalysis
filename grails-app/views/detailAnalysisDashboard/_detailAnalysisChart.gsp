<div id="dcChart">
    <div class="tabbable">
        <ul class="nav nav-tabs">
            <li class="active"><a href="#tab1" data-toggle="tab">General</a></li>
            <li><a href="#tab3" data-toggle="tab">Measured Event</a></li>
            <li><a href="#tab2" data-toggle="tab">Host</a></li>
        </ul>

        <div class="container" id="dashboardContainer">
            <div class="tab-content" style="height: 300px;">
                <div class="tab-pane active" id="tab1">
                    <div class="row" align="center" style="overflow: visible">
                        <div class="span2">
                            <div>
                                <span><strong>By MediaType</strong></span>
                            </div>

                            <div id="mediaType-chart"></div>
                        </div>

                        <div class="span2">
                            <div>
                                <span><strong>By Subtype</strong></span>
                            </div>

                            <div id="subtype-chart"></div>
                        </div>

                        <div class="span2">
                            <div>
                                <span><strong>By Browser</strong></span>
                            </div>

                            <div id="browser-chart"></div>
                        </div>

                        <div class="span2">
                            <div>
                                <span><strong>By Page</strong></span>
                            </div>

                            <div id="page-chart"></div>

                        </div>
                    </div>

                </div>

                <div class="tab-pane" id="tab3" style="overflow-x: hidden">
                    <div class="row">
                        <div class="span12">
                            <div id="measuredEvent-chart"></div>
                        </div>
                    </div>
                </div>

                <div class="tab-pane" id="tab2" style="overflow-x: hidden">
                    <div class="row">
                        <div class="span12">
                            <div id="host-chart"></div>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <div class="row">
        <div class="span12">
            <div id="dc-data-count"/>
        </div>
    </div>

    <div class="row" style="height: 400px">
        <div class="span12">
            <div id="line-chart"/>

        </div>
    </div>
</div>
