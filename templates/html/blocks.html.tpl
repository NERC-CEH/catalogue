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
        <small> - ${codes.lookup('publication.state', metadata.state)!''}</small>
      </#if>
    </h1>
  </#if>
</#macro>

<#--
Create a description block, replace any carridge returns with link breaks
-->
<#macro description value>
  <#if value?has_content>
    <p id="document-description">${value?html?replace("\n", "<br>")}</p>
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
  <div id="metadata" class="container ${classes}">
    <#nested>
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
<#macro key key description>
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
A CEH Model reference
-->
<#macro reference ref>
  <#if ref.citation?? && ref.citation?has_content>
    <@basicRow>
      <@keyContent "Citation" "Publication citation">${ref.citation}</@keyContent>
    </@basicRow>
  </#if>
  <#if ref.doi?? && ref.doi?has_content>
    <@basicRow>
      <@keyContent "DOI" "DOI link for the citation"><@bareUrl ref.doi /></@keyContent>
    </@basicRow>
  </#if>
  <#if ref.nora?? && ref.nora?has_content>
    <@basicRow>
      <@keyContent "NORA" "NORA link for the citation"><@bareUrl ref.nora /></@keyContent>
    </@basicRow>
  </#if>
</#macro>

<#--
A list of CEH Model references
-->
<#macro references references>
  <#list references as ref>
    <@basicRow "key-value">
      <div class="col-sm-12">
        <@reference ref />
      </div>
    </@basicRow>
  </#list>
</#macro>

<#-- 
A url that show repeats the url as the link text
-->
<#macro bareUrl value>
  <a href="${value}">${value}</a>
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
                ${keyword.value}
              <#else>
                  ${keyword.uri}
              </#if>
            </a>
          <#elseif keyword.value?? && keyword.value?has_content>
            ${keyword.value}
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
<#macro linebreaks content>
    ${content?replace("\n", "<br><br>")}
</#macro>

<#--
CEH model QA
-->
<#macro qa qa="">
  <#if qa?is_string>
    Unknown
  <#else>
    <div>
      <#if qa.checked?? && qa.checked?has_content>
        ${qa.checked?cap_first}
      <#else>
        Unknown
      </#if>
      <#if qa.modelVersion?? && qa.modelVersion?has_content>
        <span class="key">model version</span> ${qa.modelVersion}
      </#if>
      <#if qa.owner?? && qa.owner?has_content>
        <span class="key">owner</span> ${qa.owner}
      </#if>
      <#if qa.date?? && qa.date?has_content>
        <span class="key">date</span> ${qa.date?date}
      </#if>
    </div>
    <div>
      <#if qa.note?? && qa.note?has_content>
        <span class="key">note</span> ${qa.note}
      </#if>
    </div>
  </#if>
</#macro>

<#--
CEH model version history
-->
<#macro versionHistory history>
  <#if history.version?? && history.version?has_content>
    <@basicRow>
      <@keyContent "Version" "Model version">${history.version}</@keyContent>
    </@basicRow>
  </#if>
  <#if history.date?? && history.date?has_content>
    <@basicRow>
      <@keyContent "Date" "Version date">${history.date?date}</@keyContent>
    </@basicRow>
  </#if>
  <#if history.note?? && history.note?has_content>
    <@basicRow>
      <@keyContent "Note" "">${history.note}</@keyContent>
    </@basicRow>
  </#if>
</#macro>

<#--
A list of CEH model version history
-->
<#macro versionHistories histories>
  <#list histories as history>
    <@basicRow "key-value">
      <div class="col-sm-12">
        <@versionHistory history /> 
      </div>   
    </@basicRow>
  </#list>
</#macro>

<#--
CEH model project usage
-->
<#macro projectUsage projectUsage>
  <#if projectUsage.project?? && projectUsage.project?has_content>
    <@basicRow>
      <@keyContent "Project" "Project name and number">${projectUsage.project}</@keyContent>
    </@basicRow>
  </#if>
  <#if projectUsage.version?? && projectUsage.version?has_content>
    <@basicRow>
      <@keyContent "Version" "Model version">${projectUsage.version}</@keyContent>
    </@basicRow>
  </#if>
  <#if projectUsage.date?? && projectUsage.date?has_content>
    <@basicRow>
      <@keyContent "Date" "Date of usage">${projectUsage.date?date}</@keyContent>
    </@basicRow>
  </#if>
</#macro>

<#--
A list of CEH model project usage
-->
<#macro projectUsages projectUsages>
  <#list projectUsages as usage>
    <@basicRow "key-value">
      <@projectUsage usage />    
    </@basicRow>
  </#list>
</#macro>