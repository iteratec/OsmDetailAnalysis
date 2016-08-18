<%@ page contentType="text/html;charset=UTF-8" %>
<% def springSecurityService %>
<html>
<head>

    <meta name="layout" content="kickstart"/>
    <title><g:message code="de.iteratec.isocsi.detailAnalysisDashboard"/></title>

    <style>
    %{--Overwrite dc.min.css--}%
    .dc-chart .pie-slice {
        fill: #fff;
        font-size: 10px;
        font-weight: bold;
        cursor: pointer;
    }

    .dc-chart g.row text {
        fill: black;
    }

    .dashboardContainer {
        margin: 0 auto;
        width: 1000px;
    }

    </style>

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
            var data = ${graphData};
                var labels = ${labelAliases};
                var from = "${fromDateInMillis}" ? new Date(${fromDateInMillis}) : "";
                var to = "${toDateInMillis}" ? new Date(${toDateInMillis}) : "";
        </g:applyCodec>

        createDashboard(data, labels, from, to);
    });
    </asset:script>
</content>

</body>
</html>