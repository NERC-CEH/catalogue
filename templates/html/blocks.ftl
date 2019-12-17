<#--
Generates a title block of a metadata page. Do this by looking up the metadata
type from a code list. Also display a label if this document is currently in
draft or is pending publication
-->
<#macro title title type>
  <div class="visible-print-block text-center"><img src="/static/img/CEHlogoSmall.png"></div>

  <#if title?has_content>
    <h1 id="document-title">
      <#if type?has_content>
        <small id="resource-type">${codes.lookup('metadata.resourceType', type)!''}</small><br>
      </#if>
      ${title?html}
      <#if (metadata.state == 'draft' || metadata.state == 'pending') >
        <small class="text-danger"><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></small>
      </#if>
    </h1>
  </#if>
</#macro>

<#--
Create a description block, replace any carridge returns with link breaks
-->
<#macro description value>
  <#if value?has_content>
    <div id="document-description"><@linebreaks value!"" /></div>
  </#if>
</#macro>

<#--
Create a box of links
-->
<#macro links links title>
  <#list links>
    <div class="panel panel-default" id="document-order">
      <div class="panel-heading"><p class="panel-title">${title?html}</p></div>
      <div class="panel-body">
        <ul class="list-unstyled">
          <#items as link>
            <li><a href="${link.href}">${link.title}</a></li>
          </#items>
        </ul>
      </div>
    </div>
  </#list>
</#macro>

<#--
The basic wrapper container of a metadata document
-->
<#macro metadataContainer classes>
  <div id="metadata" class="${classes}">
    <div class="container">
      <#nested>
    </div>
  </div>
</#macro>

<#-- 
Metadata section heading
-->
<#macro sectionHeading>
   <h1 class="section-heading"><#nested></h1>
</#macro>

<#-- 
Row of a metadata document
-->
<#macro basicRow classes="">
  <div class="row ${classes}">
    <#nested>
  </div>
</#macro>

<#-- 
The basic structure of a metadata item that has a key (title) and content.
A row of information in the document.
-->
<#macro key key description auto_esc=false>
  <@basicRow "key-value">
    <@keyContent key description>
      <#nested>
    </@keyContent>
  </@basicRow>
</#macro>

<#macro keyContent key description>
  <div class="col-sm-3 key">
    <div class="key-title">
      ${key}
    </div>
    <div class="key-description">
      ${description}
    </div>
  </div>
  <div class="col-sm-9 value">
    <#nested>
  </div>
</#macro>

<#--
A repeated row
-->
<#macro repeatRow>
  <@basicRow "key-value">
    <div class="col-sm-12">
     <#nested>
    </div>
  </@basicRow>
</#macro>

<#-- 
A url that repeats the url as the link text
-->
<#macro bareUrl value>
  <a href="${value}">${value}</a>
</#macro>

<#-- 
A url that shows title as the link text
-->
<#macro titleUrl link>
  <a href="<#if link.href??>${link.href}<#else>#</#if>"><#if link.title??>${link.title}<#else>link</#if></a>
</#macro>

<#--
Block link
-->
<#macro blockUrl link>
  <div>
    <@titleUrl link/>
  </div>
</#macro>

<#--
Link url or string
-->
<#macro urlOrString link>
  <#if link.href?? && link.href?has_content>
    <@titleUrl link />
  <#elseif link.title?? && link.title?has_content>
    ${link.title}
  </#if>
</#macro>

<#-- 
A list of strings
-->
<#macro bareList values>
  <#list values>
    <ul class="list-unstyled">
      <#items as value>
        <li>${value}</li>
      </#items>
    </ul>
  </#list>
</#macro>

<#-- 
A list of Keywords
-->
<#macro keywords keywords>  
  <#list keywords>
    <ul class="list-unstyled">
      <#items as keyword>
        <li>
          <#if keyword.uri?? && keyword.uri?has_content>
            <a href="${keyword.uri}">
              <#if keyword.value?? && keyword.value?has_content>
                ${keyword.value?cap_first}
              <#else>
                  ${keyword.uri}
              </#if>
            </a>
          <#elseif keyword.value?? && keyword.value?has_content>
            ${keyword.value?cap_first}
          <#else>
            <span class="text-muted">missing</span>
          </#if>
        </li>
      </#items>
    </ul>
  </#list>
</#macro>

<#--
Replace \n with line breaks
-->
<#macro linebreaks value>
    <#if value?has_content>
      ${value?replace("\n", "<br>")}
    </#if> 
</#macro>

<#--
Admin toolbar
-->
<#macro admin>
  <#if permission.userCanEdit(id)>
    <div class="row hidden-print" id="adminPanel">
        <div class="text-right" id="adminToolbar" role="toolbar">
          <div class="btn-group btn-group-sm">
            <button type="button" class="btn btn-default btn-wide edit-control"  data-document-type="${metadata.documentType}">Edit</button>
            <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
              <span class="caret"></span>
              <span class="sr-only">Toggle Dropdown</span>
            </button>
            <ul class="dropdown-menu dropdown-menu-right">
              <li><a href="/documents/${id?html}/permission"><i class="fas fa-users"></i> Permissions</a></li>
              <li><a href="/documents/${id?html}/publication"><i class="fas fa-eye"></i> Publication status</a></li>
              <li role="separator" class="divider"></li>
              <li><a href="/documents/${id?html}/catalogue" class="catalogue-control"><i class="fas fa-sign-out-alt"></i> Move to a different catalogue</a></li>
            </ul>
            </#if>
          </div>
        </div>
    </div>
    </#if>
</#macro>