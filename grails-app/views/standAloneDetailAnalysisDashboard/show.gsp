<%@ page contentType="text/html;charset=UTF-8" %>
<% def springSecurityService %>
<html>
<head>

    <meta name="layout" content="kickstart"/>
    <title><g:message code="de.iteratec.isocsi.detailAnalysisDashboard"/></title>

</head>

<body>

<g:if test="${graphData}">

    <g:render template="/detailAnalysisDashboard/detailAnalysisChart" model="${[]}"/>
</g:if>

<content tag="include.bottom">
    <asset:script type="text/javascript">
        var OsmDetailMicroService = OsmDetailMicroService || {};
        <g:applyCodec encodeAs="none">
            OsmDetailMicroService.ajaxUrl = "${serverBaseUrl+createLink(controller:'standAloneDetailAnalysisDashboard',action:'getAssetsForDataPoint')}";
            OsmDetailMicroService.data = ${graphData};
            OsmDetailMicroService.labels = ${labelAliases};
            OsmDetailMicroService.from = "${fromDateInMillis}" ? new Date(${fromDateInMillis}) : "";
            OsmDetailMicroService.to = "${toDateInMillis}" ? new Date(${toDateInMillis}) : "";
        </g:applyCodec>
    </asset:script>
    <asset:script type="text/javascript" src="${serverBaseUrl}/assets/dashboard/DcDashboardCreator.js"></asset:script>
</content>

</body>
</html>