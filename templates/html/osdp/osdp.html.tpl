<#import "../skeleton.html.tpl" as skeleton>
<#import "../blocks.html.tpl" as b>

<#macro base>
  <@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
      <@b.metadataContainer "ceh-model">
        <div class="row"><@b.admin /></div>
        <#if title?? && title?has_content>
          <@b.key "Title" "Name of Monitoring Facility">${title}</@b.key>
        </#if>
        <#if description?? && description?has_content>
          <@b.key "Description" "Description of Monitoring Facility"><@b.linebreaks description /></@b.key>
        </#if>
        <#nested>
        <#if keywords?? && keywords?has_content>
          <@b.key "Keywords" ""><@b.keywords keywords/></@b.key>
        </#if>
      </@b.metadataContainer>
  </#escape></@skeleton.master>
</#macro>

<#macro parametersMeasured parameter>
  <@b.repeatRow>
    <#if parameter.name?? && parameter.name?has_content>
      <@b.basicRow>
        <@b.keyContent "Name" "Name of parameter">${parameter.name}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if parameter.definition?? && parameter.definition?has_content>
      <@b.basicRow>
        <@b.keyContent "Definition" "Definition of parameter">${parameter.definition}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if parameter.unitOfMeasure?? && parameter.unitOfMeasure?has_content>
      <@b.basicRow>
        <@b.keyContent "Unit of measure" "Units of parameter">${parameter.unitOfMeasure}</@b.keyContent>
      </@b.basicRow>
    </#if>
  </@b.repeatRow>
</#macro>

<#macro temporalExtent temporalExtent>
  <#if temporalExtent.begin?? && temporalExtent.begin?has_content>
    ${temporalExtent.begin}
  <#else>
    …
  </#if>
  to
  <#if temporalExtent.end?? && temporalExtent.end?has_content>
    ${temporalExtent.end}
  <#else>
    …
  </#if>
</#macro>

<#macro relationships title description relation>
  <#local links=jena.relationships(uri, relation) />
  <#if links?has_content>
    <@b.key title description>
      <#list links as link>
        <@b.blockUrl link /> 
      </#list>
    </@b.key>
  </#if>
</#macro>

<#macro inverseRelationships title description relation>
  <#local links=jena.inverseRelationships(uri, relation) />
  <#if links?has_content>
    <@b.key title description>
      <#list links as link>
        <@b.blockUrl link /> 
      </#list>
    </@b.key>
  </#if>
</#macro>