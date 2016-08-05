<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<ul class="nav pull-right">
	<li class="dropdown dropdown-btn">
		
		<a class="dropdown-toggle" data-toggle="dropdown" href="#">
    		<i class="fa fa-info-circle"></i>
			<g:message code="default.info.label" locale="${lang}"/> <b class="caret"></b>
		</a>

        <ul class="dropdown-menu">

            <li>
                <a href="https://github.com/iteratec/OsmDetailAnalysis/releases" target="_blank">
                    <i class="fa fa-github"></i>
                    <g:message code="de.iteratec.osm.releasenotes.manual.label" default="Release notes"/>
                </a>
            </li>
            <li>
                <a href="mailto:wpt@iteratec.de">
                    <i class="fa fa-envelope-o"></i>
                    <g:message code="de.iteratec.osm.contact.label" locale="${lang}"/>
                </a>
            </li>

		</ul>
	</li>
</ul>
