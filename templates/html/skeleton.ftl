<#setting url_escaping_charset='ISO-8859-1'>
<#setting date_format = 'yyyy-MM-dd'>

<#macro master title catalogue="" rdf="" schemaorg="" canonical="" searching=false can_edit_restricted=false>
<#compress>
<!DOCTYPE html>
<html lang="en-GB" <#if catalogue?has_content>data-catalogue=${catalogue.id}</#if>>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="dcterms.language" content="en">
    <meta name="dcterms.title" content="${title?html}">
    <meta name="og.title" content="${title?html}">
    <#if catalogue?has_content>
      <meta name="og.site_name" content="${catalogue.title?html}">
    </#if>
    <#if canonical?has_content>
      <meta name="og.identifier" content="${canonical}">
      <meta name="og.url" content="${canonical}">
    </#if>
    <#if description??>
        <meta name="description" content="${description?html?replace("\n", " ")}">
        <meta name="dcterms.description" content="${description?html?replace("\n", " ")}">
        <meta name="og.description" content="${description?html?replace("\n", " ")}">
    </#if>
    <title>${title?html}<#if catalogue?has_content> - ${catalogue.title?html}</#if></title>
    <link rel="stylesheet" type="text/css" href="/static/css/style-<#if catalogue?has_content>${catalogue.id?html}<#else>${catalogues.defaultCatalogue().id}</#if>.css">
    <#if canonical?has_content>
      <link rel="canonical" href="${canonical}"/>
    </#if>
    <#if rdf?has_content>
      <link rel="alternate" type="text/turtle" href="${rdf}"/>
    </#if>
    <#if schemaorg?has_content>
      <link rel="alternate" type="application/vnd.schemaorg.ld+json" href="${schemaorg}"/>
    </#if>
  <!-- HTML5 Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script type="text/javascript" src="/static/vendor/respond/respond.min.js"></script>
    <![endif]-->
  </head>
  <body data-edit-restricted="${can_edit_restricted?then('','disabled')}">
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
            <#if catalogue.id == "eidc">
              <li <#if searching>class="active"</#if>><a href="/${catalogue.id}/documents">Find data</a></li>
              <li><a href="http://eidc.ceh.ac.uk/deposit">Deposit data</a></li>
              <li><a href="http://eidc.ceh.ac.uk/support">Support</a></li>
              <li><a href="http://eidc.ceh.ac.uk/about">About</a></li>
              <li><a href="http://eidc.ceh.ac.uk/contact-info">Contact us</a></li>
              <li><a href="http://eidc.ceh.ac.uk/help">Help</a></li>
            <#else>
              <li <#if searching>class="active"</#if>><a href="/${catalogue.id}/documents">Search</a></li>
              <#if catalogue.url?? && catalogue.url == "">
                <li><h3 class="navbar-text navbar-title">${catalogue.title?html}</h3></li>
              <#else>
                <li><a href="${catalogue.url!'/'?html}">${catalogue.title?html}</a></li>
              </#if>
            </#if>
          </#if>
          </ul>
          <ul class="nav navbar-nav navbar-right">
            <#if searching && catalogue?has_content && permission.userCanCreate(catalogue.id)>
              <li>
                <div id="editorCreate" class="dropdown">
                  <button class="btn btn-default navbar-btn dropdown-toggle" type="button" id="createMenu" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                    Create
                    <span class="caret"></span>
                  </button>
                  <ul class="dropdown-menu" aria-labelledby="createMenu">
                    <#list catalogue.documentTypes as docType>
                      <li><a class="edit-control" data-document-type="${docType.type}" href="#">${docType.title}</a></li>
                    </#list>
                    <#if catalogue.fileUpload>
                      <li role="separator" class="divider"></li>
                      <li><a href="/documents/upload">File Upload</a></li>
                    </#if>
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
    <#if ! id??>
      <#include '_new-document-modal.ftl'>
    </#if>
    <script data-main="/static/scripts/main-out" src="/static/vendor/requirejs/require.js"></script>
  </body>
</html></#compress></#macro>