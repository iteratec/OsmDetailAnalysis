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
    <asset:script type="text/javascript" src="${serverBaseUrl}/assets/dashboard/DcDashboardCreator.js"></asset:script>
    <asset:script type="text/javascript">
                $(document).ready(function () {
            <g:applyCodec encodeAs="none">
                var ajaxUrl = "${serverBaseUrl+createLink(controller:'detailAnalysisDashboard',action:'getAssetsForDataPoint')}";
                var data = ${graphData};
                var labels = ${labelAliases};
                var from = "${fromDateInMillis}" ? new Date(${fromDateInMillis}) : "";
                var to = "${toDateInMillis}" ? new Date(${toDateInMillis}) : "";
            </g:applyCodec>

            createDashboard(data, labels, from, to, ajaxUrl);
        });
    </asset:script>
</content>

</body>
</html>
