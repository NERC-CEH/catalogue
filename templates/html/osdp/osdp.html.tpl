<#import "../skeleton.html.tpl" as skeleton>
<#import "../blocks.html.tpl" as b>

<#macro base>
  <@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
      <@b.metadataContainer "ceh-model">
        <@b.admin />
        <#nested>
        <#if keywords??>
          <@b.sectionHeading>Other Information</@b.sectionHeading>
          <#if keywords?has_content>
            <@b.key "Keywords" ""><@b.keywords keywords/></@b.key>
          </#if>
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
  </#if>
  to
  <#if temporalExtent.end?? && temporalExtent.end?has_content>
    ${temporalExtent.end}
  </#if>
</#macro>