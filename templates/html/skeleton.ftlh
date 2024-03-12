<#--noinspection HtmlUnknownTarget-->
<#setting url_escaping_charset='ISO-8859-1'>
<#setting date_format = 'yyyy-MM-dd'>

<#assign defaultScripts>
  <script src="/static/scripts/main.js"></script>
</#assign>

<#assign catalogueid="">

<#macro master title catalogue="" rdf="" schemaorg="" canonical="" searching=false can_edit_restricted=false containerClass="container" scripts=defaultScripts footer="">
  <#assign catalogueid="">
  <#if catalogue?has_content>
    <#assign catalogueid= catalogue.id >
  </#if>

  <#compress>
  <!DOCTYPE html>
  <html lang="en-GB" data-catalogue=${catalogueid}>
    <head>
      <meta charset="utf-8">
      <meta http-equiv="X-UA-Compatible" content="IE=edge">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <meta name="dcterms.language" content="en">
      <meta name="dcterms.title" content="${title}">
      <meta name="og.title" content="${title}">
      <#if catalogue?has_content>
        <meta name="og.site_name" content="${catalogue.title}">
      </#if>
      <#if canonical?has_content>
        <meta name="og.identifier" content="${canonical}">
        <meta name="og.url" content="${canonical}">
      </#if>
      <#if description??>
          <meta name="description" content="${description?replace("\n", " ")}">
          <meta name="dcterms.description" content="${description?replace("\n", " ")}">
          <meta name="og.description" content="${description?replace("\n", " ")}">
      </#if>
      <title>${title}<#if catalogue?has_content> - ${catalogue.title}</#if></title>
      <link rel="stylesheet" type="text/css" href="/static/css/style-<#if catalogue?has_content>${catalogue.id}<#else>${catalogues.defaultCatalogue().id}</#if>.css">
      <#if canonical?has_content>
        <link rel="canonical" href="${canonical}"/>
      </#if>
      <#if rdf?has_content>
        <link rel="alternate" type="text/turtle" href="${rdf}"/>
      </#if>
      <#if schemaorg?has_content>
        <link rel="alternate" type="application/vnd.schemaorg.ld+json" href="${schemaorg}"/>
      </#if>
    </head>
    <body data-edit-restricted="${can_edit_restricted?then('','disabled')}">
      <header>
        <div class="${containerClass}">

          <div class="toolbar">
            <div class="brand">
              <#if catalogue?has_content>
                <a href="${catalogue.url}"><img src="/static/img/cataloguelogo/${catalogue.logo}" alt="${catalogue.title}"/></a>
              <#else>
                <img src="/static/img/cataloguelogo/ukceh.png" alt="UKCEH"/>
              </#if>
            </div>
            <div class="nav-toggle">
              <i class="fa-solid fa-2x fa-bars"></i>
              <i class="fa-solid fa-2x fa-xmark"></i>
            </div>
            <div class="navigation">
              <nav class="navbar">
                <#if catalogue?has_content>
                  <a class="navbar-title" href="${catalogue.url}">${catalogue.title}</a>

                  <#if catalogue.id == "assist" || catalogue.id == 'eidc'|| catalogue.id == 'elter' || catalogue.id == "erammp">
                    <#include "menus/${catalogue.id }.ftlh">
                  <#else>
                    <a href="/${catalogue.id}/documents">Search</a>
                  </#if>
                </#if>
              </nav>
              <nav class="navbar navbar-right">

                <#if searching && catalogue?has_content && permission.userCanCreate(catalogue.id)>
                    <div id="editorCreate" class="dropdown">
                      <button class="btn btn-default dropdown-toggle" type="button" id="createMenu" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
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
                </#if>
                <span id="sso-user"></span>
              </nav>
            </div>
          </div>


        </div>
      </header>
      <main>
        <#nested>
      </main>
      <#if footer?has_content>
      <footer>
         <#include "${footer}">
      </footer>
      </#if>
      <#if catalogue?has_content && catalogue.id == "eidc">
        <script type="text/javascript" src="https://jira.ceh.ac.uk/s/d41d8cd98f00b204e9800998ecf8427e-CDN/-8wgtt1/813004/fe1f443e9c2ab11853ac2c907ebc1d28/2.2.4.7/_/download/batch/com.atlassian.plugins.jquery:jquery/com.atlassian.plugins.jquery:jquery.js?collectorId=1977b77c"></script>
        <script type="text/javascript" src="https://jira.ceh.ac.uk/s/743f218046d73ea8e8033c2a0fad57f5-T/-8wgtt1/813004/fe1f443e9c2ab11853ac2c907ebc1d28/4.0.4/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-UK&collectorId=1977b77c"></script>
      </#if>
      <div id="message-panel"></div>
      ${scripts}
    </body>
  </html></#compress>
</#macro>
