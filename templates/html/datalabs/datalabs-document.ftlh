<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

  <div id="metadata">

    <div class="">
      <div class="container">
        <#include "_admin.ftlh">
      </div>
    </div>

    <div class="section section-grey">
      <div class="container">
        <h1>${title}</h1>
        <#if owners?? && description?has_content>
          <div><@linebreaks description /></div>
        </#if>
      </div>
    </div>

    <div class="section">
      <div class="container">  
        <@displaySimple "Type" assetType />
        <@displaySimple "Version" version />
        <@displaySimple "Master URL" masterUrl />
        <@displaySimple "Primary language" primaryLanguage />
        <@displaySimple "Secondary language" secondaryLanguage />

        <#if owners?? && owners?has_content>
            <h3>Owners</h3>
            <#list owners as item>
              <div>
                <#if item.individualName??>${item.individualName}</#if>
                <#if item.organisationName??>${item.organisationName}</#if>
                </div>
            </#list>
        </#if>

        <@displaySimpleList "Inputs" inputs />
        <@displaySimpleList "Outputs" outputs />
        <@displaySimpleList "Packages" packages />
        <@displaySimpleList "Packages" packages />

        <#if keywords?has_content>
          <#assign keywords= []>
          <#list keywords as keyword>
           <#assign allKeywords = allKeywords + [keyword]>
          </#list>
        </#if>
        <#if allKeywords?has_content>
        <h3>Keywords</h3>
        <div class="keywords">
          <#list allKeywords?sort_by("value") as keyword>
            <span>
            <#if keyword.uri?has_content>
              <a href="${keyword.uri}" target="_blank" rel="noopener noreferrer">${keyword.value?trim}</a><#if keyword_has_next>,&nbsp;</#if>
            <#else>
              ${keyword.value?trim}<#if keyword_has_next>,&nbsp;</#if>
            </#if>
            </span>
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
  <#if key?? && key?has_content>
   <@displayNest key>
      ${value}
   </@displayNest>
   </#if>
</#macro>
<#macro displaySimpleList key value>
  <#if key?? && key?has_content>
   <@displayNest key>
      <#list value as item>
        <div>
          ${item} 
        </div>
      </#list>
   </@displayNest>
   </#if>
</#macro>
<#macro linebreaks value>
  <#if value?has_content>
    <#list value?split("\n") as line>
        ${line}<#sep><br></#sep>
    </#list>
  </#if>
</#macro>
</@skeleton.master>
