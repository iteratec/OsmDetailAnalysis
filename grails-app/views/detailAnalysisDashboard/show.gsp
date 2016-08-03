<%@ page contentType="text/html;charset=UTF-8" %>
<% def springSecurityService %>
<html>
<head>
   
    <title><g:message code="de.iteratec.isocsi.detailAnalysisDashboard"/></title>

    <asset:stylesheet src="bower_components/dcjs/dc.css"/>

    <style>
    %{--Overwrite dc.min.css--}%
    .dc-chart .pie-slice {
        fill: #fff;
        font-size: 10px;
        font-weight: bold;
        cursor: pointer;
    }
    </style>

</head>

<body>

<g:if test="${graphData}">
    <g:render template="/detailAnalysisDashboard/detailAnalysisChart" model="${[]}"/>
</g:if>

%{--<content tag="include.bottom">--}%
<asset:javascript src="application.js"/>
<asset:javascript src="dashboard/detailAnalysisGraph.js"/>
<asset:script type="text/javascript">

    $(document).ready(function(){
    <g:applyCodec encodeAs="none">
        var data = ${graphData};
        var labels = ${labelAliases};
        var from = "${fromDateInMillis}" ? new Date(${fromDateInMillis}) : "";
        var to = "${toDateInMillis}" ? new Date(${toDateInMillis}) : "";
    </g:applyCodec>
    drawDcGraph(data, labels, from, to, 'dcChart');
});

</asset:script>
%{--</content>--}%

<asset:deferredScripts/>
</body>
</html>
