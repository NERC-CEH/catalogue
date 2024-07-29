<#--
Generates a title block of a metadata page. Do this by looking up the metadata
type from a code list. Also display a label if this document is currently in
draft or is pending publication
-->
<#macro title title type>
  <div class="visible-print-block text-center"><img src="/static/img/cataloguelogo/ukceh.png"></div>

  <#if title?has_content>
    <h1 id="document-title">
      <#if type?has_content>
        <small id="resource-type">${codes.lookup('metadata.resourceType', type)!''}</small><br>
      </#if>
      ${title}
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
      <div class="panel-heading"><p class="panel-title">${title}</p></div>
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
<#macro key key description="" auto_esc=false>
  <@basicRow "key-value">
    <@keyContent key description>
      <#nested>
    </@keyContent>
  </@basicRow>
</#macro>

<#macro keyContent key description="">
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
<!--
 A CEH Model Application model info
-->
<#macro modelInfo modelInfo>
  <@repeatRow>
    <#if (modelInfo.id?? && modelInfo.id?has_content) || (modelInfo.name?? && modelInfo.name?has_content)>
      <#local model=jena.metadata(modelInfo.id)!""/>
      <@basicRow>
        <@keyContent "Model name" "Name of model as shown in metadata">
          <#if model?has_content>
            <a href="${model.href}">${model.title}</a>
          <#else>
            ${modelInfo.name}
            <#if modelInfo.id?? && modelInfo.id?has_content>
              (${modelInfo.id})
            </#if>
          </#if>
        </@keyContent>
      </@basicRow>
    </#if>
    <#if modelInfo.version?? && modelInfo.version?has_content>
      <@basicRow>
        <@keyContent "Version" "Version of the model used for the application (not necessarily the current release version)">${modelInfo.version}</@keyContent>
      </@basicRow>
    </#if>
    <#if modelInfo.rationale?? && modelInfo.rationale?has_content>
      <@basicRow>
        <@keyContent "Rationale" "Why was this model chosen for use in this project?">${modelInfo.rationale}</@keyContent>
      </@basicRow>
    </#if>
    <#if modelInfo.spatialExtentOfApplication?? && modelInfo.spatialExtentOfApplication?has_content>
      <@basicRow>
        <@keyContent "Spatial extent of application" "What spatial extent best describes the application?">${modelInfo.spatialExtentOfApplication?cap_first}</@keyContent>
      </@basicRow>
    </#if>
    <#if modelInfo.availableSpatialData?? && modelInfo.availableSpatialData?has_content>
      <@basicRow>
        <@keyContent "Available spatial data" "Can the application be described by either a shapefile/polygon or bounding box coordinates?">${modelInfo.availableSpatialData?cap_first}</@keyContent>
      </@basicRow>
    </#if>
    <#if modelInfo.spatialResolutionOfApplication?? && modelInfo.spatialResolutionOfApplication?has_content>
      <@basicRow>
        <@keyContent "Spatial resolution of application" "Spatial resolution at which model outputs were generated">${modelInfo.spatialResolutionOfApplication}</@keyContent>
      </@basicRow>
    </#if>
    <#if modelInfo.temporalExtentOfApplicationStartDate?? && modelInfo.temporalExtentOfApplicationStartDate?has_content>
      <@basicRow>
        <@keyContent "Temporal extent of application (start date)" "Start date of application (if applicable)">${modelInfo.temporalExtentOfApplicationStartDate}</@keyContent>
      </@basicRow>
    </#if>
    <#if modelInfo.temporalExtentOfApplicationEndDate?? && modelInfo.temporalExtentOfApplicationEndDate?has_content>
      <@basicRow>
        <@keyContent "Temporal extent of application (end date)" "End date of application (if applicable)">${modelInfo.temporalExtentOfApplicationEndDate}</@keyContent>
      </@basicRow>
    </#if>
    <#if modelInfo.temporalResolutionOfApplication?? && modelInfo.temporalResolutionOfApplication?has_content>
      <@basicRow>
        <@keyContent "Temporal resolution of application" "Time step used in the model application">${modelInfo.temporalResolutionOfApplication}</@keyContent>
      </@basicRow>
    </#if>
    <#if modelInfo.calibrationConditions?? && modelInfo.calibrationConditions?has_content>
      <@basicRow>
        <@keyContent "Calibration conditions" "How was the model calibrated (if applicable)?">${modelInfo.calibrationConditions}</@keyContent>
      </@basicRow>
    </#if>
  </@repeatRow>
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
      <#list value?split("\n") as line>
          ${line}<#sep><br></#sep>
      </#list>
    </#if>
</#macro>

<#--
Replace \n with line breaks and replace plain text http links with actionable ones
-->
<#macro linebreaksAndLinks value>
  <#if value?has_content>
    <#list value?split("\n") as line>
      <#list line?split("\\s",'r') as word>
        <#if word?matches("[\\(\\[]?http(s?)://[-a-zA-Z0-9@:%_+.~#?&/=]+[\\]\\)]?",'r')>
          <#assign link = word?replace('^\\(', '', 'ri')?replace('\\)$', '', 'ri')?replace('^\\[', '', 'ri')?replace('\\]$', '', 'ri')>
          <a href="${link}" target="_blank" rel="noopener noreferrer">${word}</a>
        <#else>
          ${word}
        </#if>
      </#list>
      <#sep><br></#sep>
    </#list>
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
              <li><a href="/documents/${id}/permission"><i class="fa-solid fa-users"></i> Permissions</a></li>
              <li><a href="/documents/${id}/publication"><i class="fa-solid fa-eye"></i> Publication status</a></li>
              <li role="separator" class="divider"></li>
              <li><a href="/documents/${id}/catalogue" class="catalogue-control"><i class="fa-solid fa-right-from-bracket"></i> Move to a different catalogue</a></li>
            </ul>
            </#if>
          </div>
        </div>
    </div>
    </#if>
</#macro>
