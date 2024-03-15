<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as b>

<#macro base>
  <@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) footer=true>
    <div id="metadata" class="${metadata.state?lower_case}">
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

          <#if description?? && description?has_content>
            <@b.linebreaksAndLinks description />
          </#if>

        </div>
      </div>
      <div class="section">
        <div class="container">
          <#nested>

          <#if keywords?? && keywords?has_content>
            <@keyValue "Keywords" ""><@b.keywords keywords/></@keyValue>
          </#if>
        </div>
      </div>
    </#if>
    </div>
  </@skeleton.master>
</#macro>

<#macro keyValue key description="" auto_esc=false>
  <div class="row key-value" title="${description}">
    <div class="col-sm-3 key">
      <div class="key-title">${key}</div>
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
Temporal Extent
-->
<#macro temporalExt temporalExtent>
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
    <@geometry geometry=combinedGeometry />
  </#if>
</#macro>
