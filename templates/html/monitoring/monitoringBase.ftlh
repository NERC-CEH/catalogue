<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as b>
<#import "../../functions.ftlh" as func>

<#macro base>
  <@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) footer=true>
    <div id="metadata" class="metadata-monitoring ${metadata.state?lower_case}">
    <#if !type??>
    <#else>
      <div class="section section-grey">
        <div class="container">
          <div ><@b.admin /></div>
          <#if title?? && title?has_content>
            <h1>
               <small>
                <#if (metadata.state == 'draft' || metadata.state == 'pending') >
                  <span class="text-danger"><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></span>
                </#if>
               ${codes.lookup('metadata.resourceType', type)!''}<br>
              </small>
              ${title}
            </h1>
          </#if>

          <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
            <div class="section">
              <div class="container">
                <#include "../document/_metadataqualityAlert.ftlh">
              </div>
            </div>
          </#if>

          <#if description?? && description?has_content>
            <div><@b.linebreaksAndLinks description /></div>
          </#if>

        </div>
      </div>
      <div class="section">
        <div class="container">
          <#nested>

          <#if environmentalDomain??>
           <@keyValue "Environmental domain" ""><@displayKeywords environmentalDomain/></@keyValue>
          </#if>

          <#if purposeOfCollection??>
             <@keyValue "Purpose of collection" ""><@displayKeywords purposeOfCollection/></@keyValue>
          </#if>


          <#if objectives?? && objectives?has_content>
             <@keyValue "Objectives" ""><@b.linebreaksAndLinks objectives /></@keyValue>
          </#if>

          <#if operatingPeriod?has_content>
            <@keyValue "Operating period" "">
              <#list operatingPeriod>
                <#items as op>
                  <@displayPeriod op /><#sep><br></#sep>
                </#items>
               </#list>
            </@keyValue>
          </#if>

          <#if responsibleParties?has_content>
            <div>
              <h2>Contacts</h2>

              <#list responsibleParties>
                <#items as rp>
                  <@contactDetails rp/>
                </#items>
              </#list>

            </div>
          </#if>

          <#if linksData?has_content>
            <@displayLinks linksData "Links to data"/>
          </#if>

          <#if linksOther?has_content>
            <@displayLinks linksOther "Other links"/>
          </#if>

          <div>
            <h2>Other metadata</h2>

            <#if keywordsParameters?? && keywordsParameters?has_content>
              <@keyValue "Parameters measured" ""><@displayKeywords keywordsParameters ","/></@keyValue>
            </#if>

            <#if keywords?? && keywords?has_content>
              <@keyValue "Keywords" ""><@displayKeywords keywords ","/></@keyValue>
            </#if>

            <#if metadataDateTime?has_content>
              <@keyValue "Last updated" "">${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</@keyValue>
            </#if>
          </div>

        </div>
        <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
          <div class="section">
            <div class="container">
              <#include "../document/_metadataqualityReport.ftlh">
            </div>
          </div>
        </#if>
      </div>
    </#if>
    </div>
  </@skeleton.master>
</#macro>

<#macro keyValue key description="" auto_esc=false>
  <div class="key-value" title="${description}">
    <div class="col-sm-3 key">
     ${key}
    </div>
    <div class="col-sm-9 value">
      <#nested>
    </div>
  </div>
</#macro>

<#macro supplemental supplementalList>
  <#list supplementalList>
    <#items as sup>
      <#if sup.function?has_content?has_content>
        <#assign key = sup.function?capitalize>
      <#else>
          <#assign key = "Other">
      </#if>
      <@keyValue "${key}"><@b.linebreaksAndLinks sup.description /></@keyValue>
    </#items>
  </#list>
</#macro>

<#--
ReferenceDates
-->
<#macro refDate dates>
  <@b.repeatRow>
    <#if dates.creationDate?? && dates.creationDate?has_content>
      <@b.basicRow>
        <@b.keyContent "Creation Date" "Artifact creation date">${dates.creationDate}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if dates.publicationDate?? && dates.publicationDate?has_content>
      <@b.basicRow>
        <@b.keyContent "Publication Date" "Artifact publication date">${dates.publicationDate}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if dates.revisionDate?? && dates.revisionDate?has_content>
      <@b.basicRow>
        <@b.keyContent "Revision Date" "Artifact revision date">${dates.revisionDate}</@b.keyContent>
      </@b.basicRow>
    </#if>
  </@b.repeatRow>
</#macro>

<#--
ParametersMeasured
-->
<#macro parametersMeasured parameter>
  <@b.repeatRow>
    <#if parameter.name?? && parameter.name?has_content>
      <@b.basicRow>
        <@b.keyContent "Name" "Name of parameter"><@b.urlOrString parameter.name /></@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if parameter.unitOfMeasure?? && parameter.unitOfMeasure?has_content>
      <@b.basicRow>
        <@b.keyContent "Unit of measure" "Units of parameter"><@b.urlOrString parameter.unitOfMeasure /></@b.keyContent>
      </@b.basicRow>
    </#if>
  </@b.repeatRow>
</#macro>

<#--
Operating period
-->
<#macro displayPeriod period>
  <#if period.begin?? && period.begin?has_content>
    ${period.begin}
  <#else>
    …
  </#if>
  to
  <#if period.end?? && period.end?has_content>
    ${period.end}
  <#else>
    …
  </#if>
</#macro>

<#macro relationships title description relation> <#--UPDATE-->
  <#local links=jena.relationships(uri, relation) />
  <#if links?has_content>
    <@keyValue description "">
      <#list links as link>
        <@b.blockUrl link />
      </#list>
    </@keyValue>
  </#if>
</#macro>

<#macro inverseRelationships title description relation> <#--UPDATE-->
  <#local links=jena.inverseRelationships(uri, relation) />
  <#if links?has_content>
    <@keyValue description  "">
      <#list links as link>
        <@b.blockUrl link />
      </#list>
    </@keyValue>
  </#if>
</#macro>

<#macro map mapData>
  <div id="studyarea-map">
    <span content="${mapData}" dataType="geoJson"/>
  </div>
</#macro>

<#macro inverseRelationshipCombinedGeometries relation>
  <#local combinedGeometry=jena.inverseRelationshipCombinedGeometries(uri, relation) />
  <#if combinedGeometry?has_content>
    <@map combinedGeometry />
  </#if>
</#macro>

<#macro contactDetails contact>
 <@keyValue contact.role?capitalize "">
    <#if contact.individualName?has_content>
      <span>
        ${contact.individualName}
        <#if contact.nameIdentifier?has_content>
          <a href="${contact.nameIdentifier}" target="_blank" rel="noopener noreferrer">
            <img src="/static/img/orcid_16x16.png" alt="ORCID iD icon" title="ORCID iD">
          </a>
        </#if>
      </span>
      <br>
    </#if>

    <#if contact.organisationName?has_content>
      <span>
        ${contact.organisationName}
        <#if contact.organisationIdentifier?has_content>
          <a href="${contact.organisationIdentifier}" target="_blank" rel="noopener noreferrer">
            <img src="/static/img/ror_16x16.png" alt="RoR icon" title="Research Organisation Identifier">
          </a>
        </#if>
      </span>
      <br>
    </#if>
  </@keyValue>
</#macro>

<#macro displayLinks linkList title="">
  <#list linkList>
    <@keyValue "${title}" "">
      <#items as link>
        <#if link.url?has_content>
          <a href="${link.url}" target="_blank" rel="noopener noreferrer">${link.description}</a>
        <#else>
          ${link.description}
        </#if><#sep><br></#sep>
      </#items>
    </@keyValue>
  </#list>
</#macro>

<#macro displayKeywords keywords separator="<br>">
  <#list keywords as keyword>

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

    <#sep>${separator?no_esc}</#sep>
  </#list>
</#macro>
