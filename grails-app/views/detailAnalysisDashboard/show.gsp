<%@ page contentType="text/html;charset=UTF-8" %>
<% def springSecurityService %>
<html>
<head>

    <meta name="layout" content="kickstart_osm_da"/>
    <title><g:message code="de.iteratec.isocsi.detailAnalysisDashboard"/></title>


</head>

<body>

<g:if test="${graphData}">

    <g:render template="/detailAnalysisDashboard/detailAnalysisChart" model="${[]}"/>
</g:if>

<content tag="include.bottom">
    <asset:script type="text/javascript">
        var OsmDetailMicroService = OsmDetailMicroService || {};
        var OpenSpeedMonitor = OpenSpeedMonitor || {};
        OpenSpeedMonitor.postLoadUrls = OpenSpeedMonitor.postLoadUrls || [];
        OpenSpeedMonitor.postLoadUrls.push("${serverBaseUrl}/assets/dashboard/DcDashboardCreator.js");
        <g:applyCodec encodeAs="none">
            OsmDetailMicroService.ajaxUrl = "${serverBaseUrl+createLink(controller:'detailAnalysisDashboard',action:'getAssetsForDataPoint')}";
            OsmDetailMicroService.data = ${graphData};
            OsmDetailMicroService.labels = ${labelAliases};
            OsmDetailMicroService.from = "${fromDateInMillis}" ? new Date(${fromDateInMillis}) : "";
            OsmDetailMicroService.to = "${toDateInMillis}" ? new Date(${toDateInMillis}) : "";
            OsmDetailMicroService.i18n = ${i18n};
        </g:applyCodec>
    </asset:script>
</content>

</body>
</html>
