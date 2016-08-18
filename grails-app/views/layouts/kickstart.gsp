<!DOCTYPE html>
<html lang="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}">

    <head>

        <asset:stylesheet src="application.css"/>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><g:layoutTitle default="${meta(name:'app.name')}" /></title>
        <meta charset="utf-8">
        <meta name="viewport"		content="width=device-width, initial-scale=1.0">
        <meta name="description"	content="">
        <meta name="author"			content="">

        <asset:link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>

        <link rel="apple-touch-icon"	href="assets/ico/apple-touch-icon.png">
        <link rel="apple-touch-icon"	href="assets/ico/apple-touch-icon-72x72.png"	sizes="72x72">
        <link rel="apple-touch-icon"	href="assets/ico/apple-touch-icon-114x114.png"	sizes="114x114">

        <g:layoutHead />

    </head>

    <body>


        <!-----------------------------------------------------body-->
        <g:render template="/layouts/content_osm"/>

        <asset:javascript src="application.js"/>

        <!-----------------------------------------------------body bottom block for javascripts of root pages-->
        <g:if test="${ pageProperty(name:'page.include.bottom') }">
            <g:pageProperty name="page.include.bottom" />
        </g:if>

        <!-----------------------------------------------------render all deferred scripts -->
        <asset:deferredScripts/>

        %{--<!-----------------------------------------------------global postload-Javascript-->--}%
        %{--<g:render template="/_common/postloadInitializedJS"/>--}%

    </body>

</html>