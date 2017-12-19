<#import "../skeleton.html.tpl" as skeleton>
<#import "../blocks.html.tpl" as b>
<#import "/../../functions.tpl" as func>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
      <@b.metadataContainer "ceh-model">
        <div class="row"><@b.admin /></div>
        <#if title?? && title?has_content>
          <@b.key "Title" "">${title}</@b.key>
        </#if>
        <#if type?? && type?has_content>
          <@b.key "Type" "">${codes.lookup('metadata.resourceType', type)!''}</@b.key>
        </#if>
        <#if description?? && description?has_content>
          <@b.key "Description" ""><@b.linebreaks description /></@b.key>
        </#if>
        <#if archiveType?? && archiveType?has_content>
          ${archiveType}
        </#if>
        <#if lineage?? && lineage?has_content>
          <@b.key "Lineage" "">${lineage}</@b.key>
        </#if>
        <#if language?? && language?has_content>
          <@b.key "Language" "">${language}</@b.key>
        </#if>
        <#if specimenTypes?? && specimenTypes?has_content>
          <@b.key "Specimen Types" ""><@b.keywords specimenTypes/></@b.key>
        </#if>
        <#if keywords?? && keywords?has_content>
          <@b.key "Keywords" ""><@b.keywords keywords/></@b.key>
        </#if>
        <#if topicCategories?? && topicCategories?has_content>
          <@b.key "Topic Categories" ""><@b.keywords topicCategories/></@b.key>
        </#if>
        <#include "_extent.html.tpl">
        <#if availability?? && availability?has_content>
          <@b.key "Availability" "">${availability}</@b.key>
        </#if>
        <#if accessRestrictions?? && accessRestrictions?has_content>
          <@b.key "Access Restrictions" "">${accessRestrictions}</@b.key>
        </#if>
        <#if storage?? && storage?has_content>
          <@b.key "Storage" "">${storage}</@b.key>
        </#if>
        <#if healthSafety?? && healthSafety?has_content>
          <@b.key "Health and Safety" "">${healthSafety}</@b.key>
        </#if>
        <#if archiveLocations?? && archiveLocations?has_content>
          <@b.key "Archive Location${(archiveLocations?size > 1)?then('s', '')}" "">
            <#list archiveLocations as archiveLocation>
              <@b.key "Address" + (archiveLocations?size > 1)?then(" " + (archiveLocation?index + 1),'') "">
                <#noescape>${archiveLocation?html?replace("\n", "<br>")}</#noescape>
              </@b.key>
            </#list>
          </@b.key>
        </#if>
        <#if resourceIdentifiers?? && resourceIdentifiers?has_content>
          <@b.key "Identifier" "">
            <#list resourceIdentifiers as resourceIdentifier>
              <@identifier resourceIdentifier /> 
            </#list>
          </@b.key>
        </#if>
        <#include "_pointsOfContact.html.tpl" />
        <#if metadataDate?? && metadataDate?has_content>
          <@b.sectionHeading>Metadata</@b.sectionHeading>
          <@b.key "Metadata Date" "Date metadata last updated">${metadataDateTime}</@b.key>
        </#if>
        <#if website?? && website?has_content>
          <@b.key "Website" "">${website}</@b.key>
        </#if>
      </@b.metadataContainer>
  </#escape></@skeleton.master>

  <#--
Identifiers
-->
<#macro identifier ident>
  <@b.repeatRow>
    <#if ident.code?? && ident.code?has_content>
      <@b.basicRow>
        <@b.keyContent "Code" "">${ident.code}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if ident.codeSpace?? && ident.codeSpace?has_content>
      <@b.basicRow>
        <@b.keyContent "Codespace" "">${ident.codeSpace}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if ident.version?? && ident.version?has_content>
      <@b.basicRow>
        <@b.keyContent "Version" "">${ident.version}</@b.keyContent>
      </@b.basicRow>
    </#if>
  </@b.repeatRow>
</#macro>
