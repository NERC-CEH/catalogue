<#setting url_escaping_charset='ISO-8859-1'>
<#setting date_format = 'yyyy-MM-dd'>

<#macro master title catalogue="" rdf="" searching=false><#compress><!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title?html}<#if catalogue?has_content> - ${catalogue.title?html}</#if></title>
    <link rel="stylesheet" type="text/css" href="/static/css/style-<#if catalogue?has_content>${catalogue.id?html}<#else>${catalogues.defaultCatalogue().id}</#if>.css">
    <#if rdf?has_content>
      <link rel="meta" type="application/rdf+xml" href="${rdf}"/>
    </#if>

  <!-- HTML5 Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script type="text/javascript" src="/static/vendor/respond/respond.min.js"></script>
    <![endif]-->
  </head>
  <body>
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
      <div class="container-fluid">
        <div id="sso-brand" class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <h3 class="navbar-text" href="#">Not Proxied</h3>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <#if catalogue?has_content>
              <li <#if searching>class="active"</#if>><a href="/${catalogue.id}/documents">Search Data</a></li>
            </#if>
            <li><a href="//eip.ceh.ac.uk/catalogue/help">Help</a></li>
            <#if catalogue?has_content>
              <li><a href="${catalogue.url!'/'?html}">${catalogue.title?html}</a></li>
            </#if>
          </ul>
          <ul class="nav navbar-nav navbar-right">
            <#if searching && permission.userCanCreate()>
              <li>
                <div id="editorCreate" class="dropdown">
                  <button class="btn btn-default navbar-btn dropdown-toggle" type="button" id="createMenu" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                    Create
                    <span class="caret"></span>
                  </button>
                  <ul class="dropdown-menu" aria-labelledby="createMenu">
                    <li><a class="edit-control" data-document-type="GEMINI_DOCUMENT" href="#">Data Resource</a></li>
                    <li><a class="edit-control" data-document-type="EF_DOCUMENT" href="#">Monitoring</a></li>
					          <li><a class="edit-control" data-document-type="IMP_DOCUMENT" href="#">Model</a></li>
                    <li><a class="edit-control" data-document-type="LINK_DOCUMENT" href="#">Link</a></li>
                    <li role="separator" class="divider"></li>
                    <li><a href="/documents/upload">File Upload</a></li>
                  </ul>
                </div>
              </li>
            </#if>
            <li id="sso-user"><a>Joe Bloggs</a></li>
          </ul>
        </div>
      </div>
    </div>
    <#nested>
    <div id="message-panel"></div>
    <script data-main="/static/scripts/main-out" src="/static/vendor/requirejs/require.js"></script>
  </body>
</html></#compress></#macro>