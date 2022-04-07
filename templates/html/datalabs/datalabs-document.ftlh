<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as f>

<#if useConstraints?has_content>
  <#assign licences = f.filter(useConstraints, "code", "license")>
</#if>
<#if responsibleParties?has_content>
  <#assign 
    owners = f.filter(responsibleParties, "role", "owner")
    contacts = f.filter(responsibleParties, "role", "pointOfContact")
    >
</#if>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

  <div id="metadata">
    <#include "_admin.ftlh">

    <div class="section section-grey">
      <div class="container">
        <h1>${title}</h1>
        <#if description?? && description?has_content>
          <div><@linebreaks description /></div>
        </#if>
      </div>
    </div>

    <div class="section">
      <div class="container">

      <#if assetType?? && assetType?has_content>
        <@displaySimple "Type" assetType />
      </#if>

        <#if owners?? && owners?has_content>
            <h3>Owner<#if owners?size gt 1>s</#if></h3>
            <#list owners as item>
              <div>
                <#if item.individualName??>${item.individualName}</#if>
                <#if item.organisationName??>${item.organisationName}</#if>
                </div>
            </#list>
        </#if>

        <#if contacts?? && contacts?has_content>
            <h3>Contact<#if contacts?size gt 1>s</#if></h3>
            <#list contacts as item>
              <div>
                <#if item.individualName??>${item.individualName}</#if>
                <#if item.organisationName??>${item.organisationName}</#if>
                </div>
            </#list>
        </#if>

        <#if version?? && version?has_content>
          <@displaySimple "Version" version />
        </#if>
        <#if masterUrl?? && masterUrl?has_content>
          <@displaySimple "Master URL" masterUrl />
        </#if>
        <#if primaryLanguage?? && primaryLanguage?has_content>
          <@displaySimple "Primary language" primaryLanguage />
        </#if>
        <#if secondaryLanguage?? && secondaryLanguage?has_content>
          <@displaySimple "Secondary language" secondaryLanguage />
        </#if>

        <#if inputs?? && inputs?has_content>
          <@displaySimpleList "Inputs" inputs />
        </#if>
        <#if outputs?? && outputs?has_content>
          <@displaySimpleList "Outputs" outputs />
        </#if>
        <#if packages?? && packages?has_content>
          <@displaySimpleList "Packages" packages />
        </#if>

        <#if keywords?has_content>
        <h3>Keyword<#if keywords?size gt 1>s</#if></h3>
        <div class="keywords">
          <#list keywords?sort_by("value") as keyword>
            <#if keyword.uri?has_content>
              <a href="${keyword.uri}" target="_blank" rel="noopener noreferrer">${keyword.value?trim}</a><#sep>,&nbsp;</#sep>
            <#else>
              ${keyword.value?trim}<#sep>,&nbsp;</#sep>
            </#if>
          </#list>
        </div>
        </#if>
        
        <#if inspireThemes?has_content>
        <h3>INSPIRE theme<#if inspireThemes?size gt 1>s</#if></h3>
        <div class="inspireThemes">
          <#list inspireThemes?sort_by("theme") as theme>
            <#if theme.uri?has_content>
              <a href="${theme.uri}" target="_blank" rel="noopener noreferrer">${theme.theme?trim}</a><#sep><br></#sep>
            <#else>
              ${theme.value?trim}<#sep><br></#sep>
            </#if>
          </#list>
        </div>
        </#if>

        <#if boundingBoxes?has_content>
          <div>
            <h3>Spatial extent</h3>
              <div id="studyarea-map">
                <#list boundingBoxes as extent>
                  <span content="${extent.wkt}" datatype="geo:wktLiteral"/>
                </#list>
              </div>
          </div>
        </#if>

        <#if temporalExtents?has_content>
          <div>
          <h3>Temporal extent</h3>
            <#list temporalExtents as extent>
                <#if extent.begin?has_content>
                  <span class="extentBegin">${extent.begin?date}</span>
                <#else>
                  &hellip;
                </#if>
                &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;
                <#if extent.end?has_content>
                  <span class="extentEnd">${extent.end?date}</span>
                <#else>
                  &hellip;
                </#if>
                <#sep><br>&nbsp;&nbsp;&nbsp;&nbsp;and <br></#sep>
            </#list>
          </div>
        </#if>


        <@displayNest "Last updated">
          ${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}
        </@displayNest>

        </div>
    </div>

  </div>


<#macro displayNest key>
  <div>
    <h3>${key}</h3>
    <#nested>
  </div>
</#macro>

<#macro displaySimple key value>
   <@displayNest key>
      ${value}
   </@displayNest>
</#macro>

<#macro displaySimpleList key value>
   <@displayNest key>
      <#list value as item>
        <div>
          ${item} 
        </div>
      </#list>
   </@displayNest>
</#macro>

<#macro linebreaks value>
  <#if value?has_content>
    <#list value?split("\n") as line>
        ${line}<#sep><br></#sep>
    </#list>
  </#if>
</#macro>
</@skeleton.master>
