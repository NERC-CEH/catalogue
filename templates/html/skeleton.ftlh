<#--noinspection HtmlUnknownTarget-->
<#assign defaultScripts>
  <script src="/static/scripts/main.js"></script>
</#assign>

<#macro master title catalogue="" canonical="" searching=false can_edit_restricted=false containerClass="container" scripts=defaultScripts footer=false headContent="">
  <#assign catalogueid=catalogue?has_content?then(catalogue.id, "")>

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
      <#-- Declared explicitly so browsers do not fall back to probing /favicon.ico at the root. -->
      <#if catalogue?has_content>
        <link rel="icon" href="/static/img/cataloguelogo/${catalogue.logo}">
      <#else>
        <link rel="icon" href="/static/img/cataloguelogo/ukceh.png">
      </#if>
      <link rel="stylesheet" type="text/css" href="/static/css/style-<#if catalogue?has_content>${catalogue.id}<#else>${catalogues.defaultCatalogue().id}</#if>.css">

      <#if searching && catalogue?has_content && permission.userCanCreate(catalogue.id)>
        <link rel="stylesheet" type="text/css" href="/static/css/style-admin.css">
      </#if>

      <#if canonical?has_content>
        <link rel="canonical" href="${canonical}"/>
      </#if>
      <#if voidCatalogueIds?? && voidCatalogueIds?seq_contains(catalogueid)>
      <link rel="describedby" type="text/turtle" href="/.well-known/void"/>
      </#if>
      ${headContent}
    </head>
    <body data-edit-restricted="${can_edit_restricted?then('','disabled')}">
      <div id="header-sentinel" aria-hidden="true"></div>
      <header>
        <div class="${containerClass}">
          <div class="toolbar">
            <div class="nav-toggle">
              <i class="fa-solid fa-2x fa-bars"></i>
              <i class="fa-solid fa-2x fa-xmark"></i>
            </div>
            <div class="navigation">
              <div class="siteLogo">
                <#if catalogue?has_content>
                  <a href="${catalogue.url}"><img src="/static/img/cataloguelogo/${catalogue.logo}" alt="${catalogue.title}"/></a>
                <#else>
                  <img src="/static/img/cataloguelogo/ukceh.png" alt="UKCEH"/>
                </#if>
              </div>
              <nav class="navbar">
                <#if catalogue?has_content>
                  <a class="navbar-title" href="${catalogue.url}">${catalogue.title}</a>

                  <#if ["assist", "eidc"]?seq_contains(catalogue.id)>
                    <#include "menus/${catalogue.id}.ftlh">
                  <#else>
                    <a href="/${catalogue.id}/documents">Search</a>
                  </#if>
                </#if>
              </nav>
              <nav class="navbar navbar-right">

                <#if searching && catalogue?has_content && permission.userCanCreate(catalogue.id)>
                    <div id="editorCreate" class="dropdown">
                      <button class="btn btn-outline-secondary btn-create dropdown-toggle" type="button" id="createMenu" data-bs-toggle="dropdown" aria-expanded="false">
                        Create
                      </button>
                      <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="createMenu">
                        <#list catalogue.documentTypes as docType>
                          <li><a class="dropdown-item edit-control" data-document-type="${docType.type}" href="#">${docType.title}</a></li>
                        </#list>
                        <#if catalogue.fileUpload>
                          <li><hr class="dropdown-divider"></li>
                          <li><a class="dropdown-item" href="/documents/upload">File Upload</a></li>
                        </#if>
                      </ul>
                    </div>
                </#if>
                <#if profile.isActive("auth:oidc")>
                  <#if userInfo.isLoggedIn()>
                    <a href="/logout">Logout</a>
                  <#else>
                    <a href="/login">Login</a>
                  </#if>
                <#elseif profile.isActive("auth-cognito")>
                  <#if userInfo.isLoggedIn()>
                    <a href="/logout">Logout with Cognito</a>
                  <#else>
                    <a href="/oauth2/authorization/cognito">Login with Cognito</a>
                  </#if>
                <#else>
                  <#-- SSO user menu, rendered natively (formerly injected by the proxy
                       into #sso-user). Links target the proxy-served /sso/* endpoints. -->
                  <#if userInfo.isLoggedIn()>
                    <div class="dropdown user-menu">
                      <a href="#" role="button" class="dropdown-toggle" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false" title="Your details">
                        <i class="fa-solid fa-user"></i>
                      </a>
                      <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item" href="/sso/password/change"><i class="fa-solid fa-key fa-fw"></i>Change password</a></li>
                        <li><a class="dropdown-item" href="/sso/profile"><i class="fa-solid fa-user fa-fw"></i>Profile</a></li>
                        <li>
                          <form action="/sso/logout" method="post">
                            <button type="submit" class="dropdown-item text-danger"><i class="fa-solid fa-right-from-bracket fa-fw"></i>Logout</button>
                          </form>
                        </li>
                      </ul>
                    </div>
                  <#else>
                    <a href="/sso/login">Login</a>
                  </#if>
                </#if>
              </nav>
            </div>
          </div>
        </div>
      </header>
      <main>
        <#nested>
        <div id="message-panel"></div>
      </main>
      <#if footer>
        <footer>

          <#if metadataDateTime?has_content>
            <div class="footer-subsection">
              <div class="container">
                <div class="small"><b>Last updated</b> ${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</div>
              </div>
            </div>
          </#if>

          <#if catalogue.id == "eidc">
            <#include "footer/eidc.ftlh">
          <#elseif catalogue.id == "ukeof">
            <#include "footer/ukeof.ftlh">
          <#else>
            <#include "footer/ukceh.ftlh">
          </#if>
        </footer>
      </#if>
      <#if catalogue?has_content && catalogue.id == "eidc">
        <script type="text/javascript" src="https://jira.ceh.ac.uk/s/d41d8cd98f00b204e9800998ecf8427e-CDN/-8wgtt1/813004/fe1f443e9c2ab11853ac2c907ebc1d28/2.2.4.7/_/download/batch/com.atlassian.plugins.jquery:jquery/com.atlassian.plugins.jquery:jquery.js?collectorId=1977b77c"></script>
        <script type="text/javascript" src="https://jira.ceh.ac.uk/s/743f218046d73ea8e8033c2a0fad57f5-T/-8wgtt1/813004/fe1f443e9c2ab11853ac2c907ebc1d28/4.0.4/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-UK&collectorId=1977b77c"></script>
      </#if>
      ${scripts}
    </body>
  </html>
  </#compress>
</#macro>
