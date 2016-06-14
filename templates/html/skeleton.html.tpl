<#setting url_escaping_charset='ISO-8859-1'>
<#setting date_format = 'yyyy-MM-dd'>

<#macro master title="" catalogue="" rdf="" searching=false><#compress><!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title?html} - ${catalogue?html}</title>
    <link rel="stylesheet" type="text/css" href="/static/css/style.css">
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
            <li><a href="//eip.ceh.ac.uk">Home</a></li>
            <li <#if searching>class="active"</#if>><a href="/documents">Search Data</a></li>
            <li><a href="//eip.ceh.ac.uk/catalogue/help">Help</a></li>
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
                    <li><a class="edit-control gemini" href="#">Data Resource</a></li>
                    <li><a class="edit-control monitoring" href="#">Monitoring</a></li>
                    <li role="separator" class="divider"></li>
                    <li><a class="edit-control" href="/documents/upload">File Upload</a></li>
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