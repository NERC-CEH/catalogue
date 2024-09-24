<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as b>
<#import "../../functions.ftlh" as func>

<#if onlineResources?? && onlineResources?has_content>
  <#assign
    image = func.filter(onlineResources, "function", "browseGraphic")
    otherLinks = func.filter(onlineResources, "function", "browseGraphic", true)
  >
</#if>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <div id="metadata" class="metadata sampleArchive-metadata">
    <div class="section section-grey">
      <div class="container">
        <@b.admin />

        <div id="pageTitle">
          <#if title?? && title?has_content>
            <h1>${title}</h1>
          </#if>
        </div>
        <div class="description">

          <#if description?? && description?has_content>
            <div class="description-text">
              <@b.linebreaks description />
            </div>
          </#if>

          <div class="browseimage">
            <#if image?? && image?has_content>
              <#if image?first.url?matches("^.+(jpg|jpeg|gif|png)$")>
                <img src="${image?first.url}" alt="${image?first.name}" class="browseGraphic" />
              </#if>
            </#if>
          </div>
        </div>

      </div>
    </div>

    <div class="section">
      <div class="container">
        <div class="metadata-grid">
          <div class="col1">

            <#if archiveLocations?? && archiveLocations?has_content>
              <@key "Location${(archiveLocations?size > 1)?then('s', '')}">
                <#list archiveLocations as contact>
                  <div class="responsibleParty">
                    <@func.displayContact contact, true, true />
                  </div>
                </#list>
              </@key>
            </#if>

            <#if website?? && website?has_content>
              <@key "Website"><a href="${website}">${website}</a></@key>
            </#if>

            <#if availability?? && availability?has_content>
              <@key "Availability">${availability}</@key>
            </#if>
            <#if accessRestrictions?? && accessRestrictions?has_content>
              <@key "Access restrictions">${accessRestrictions}</@key>
            </#if>
            <#if storage?? && storage?has_content>
              <@key "Storage">${storage}</@key>
            </#if>
            <#if healthSafety?? && healthSafety?has_content>
              <@key "Health & safety">${healthSafety}</@key>
            </#if>
            <#if specimenTypes?? && specimenTypes?has_content>
              <@key "Specimen type${(specimenTypes?size > 1)?then('s', '')}"><@tags specimenTypes/></@key>
            </#if>
            <#if taxa?? && taxa?has_content>
              <@key "Taxa"><@tags taxa/></@key>
            </#if>
            <#if tissues?? && tissues?has_content>
              <@key "Tissue${(tissues?size > 1)?then('s', '')}"><@tags tissues/></@key>
            </#if>
            <#if physicalStates?? && physicalStates?has_content>
              <@key "Physical state${(physicalStates?size > 1)?then('s', '')}"><@tags physicalStates/></@key>
            </#if>
            <#if keywords?? && keywords?has_content>
              <@key "Additional keyword${(keywords?size > 1)?then('s', '')}"><@tags keywords/></@key>
            </#if>
            <#if boundingBoxes?? && boundingBoxes?has_content>
              <@key "Geographic coverage${(boundingBoxes?size > 1)?then('s', '')}" >
                <div id="section-extent">
                  <div id="studyarea-map" class="studyarea-map">
                  <#list boundingBoxes as extent>
                      <span content="${extent.bounds}" datatype="geoJson"/>
                  </#list>
                  </div>
                </div>
              </@key>
            </#if>

            <#if temporalExtent??>
              <@key "Temporal coverage">
                <div class="temporalExtent">
                <#if temporalExtent.begin?has_content>
                  <span class="extentBegin">${temporalExtent.begin?date.iso?date?string['MMMM yyyy']}</span>
                <#else>
                  &hellip;
                </#if>
                &nbsp; to &nbsp;
                <#if temporalExtent.end?has_content>
                  <span class="extentEnd">${temporalExtent.end?date.iso?date?string['MMMM yyyy']}</span>
                <#else>
                  present day
                </#if>
                </div>
              </@key>
            </#if>

            <#if lineage?? && lineage?has_content>
              <@key "Lineage">${lineage}</@key>
            </#if>

            <#if funding??>
              <@key "Funding">
                <#list funding as fund>
                  <#if fund.funderName?has_content >
                    ${fund.funderName}
                  </#if>
                  <#if fund.awardNumber?has_content >
                      <#if fund.awardURI?has_content>
                        <a href="${fund.awardURI}" target="_blank" rel="noopener noreferrer">Award: ${fund.awardNumber}</a>
                      <#else>
                        Award: ${fund.awardNumber}
                      </#if>
                  </#if>
                  <#sep><br></#sep>
                </#list>
              </@key>
            </#if>

            <#if onlineResources?? && onlineResources?has_content>
              <@key "Further information">
                <#list onlineResources as onlineResource>
                  <div><a href="${onlineResource.url}" target="_blank" rel="noopener noreferrer">${onlineResource.name}</a></div>
                </#list>
              </@key>
            </#if>

            <div class="record-metadata">
              <#if metadataContacts?? && metadataContacts?has_content>
                <@key "Author of this record">
                  <#list metadataContacts as contact>
                    <div class="responsibleParty">
                      <@func.displayContact contact, true, true />
                    </div>
                  </#list>
                </@key>
              </#if>

              <#if metadataDate?? && metadataDate?has_content>
                  <@key "Record last updated">${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</@key>
              </#if>

              <@key "Permanent link">
                  <a href="https://catalogue.ceh.ac.uk/documents/${id}">https://catalogue.ceh.ac.uk/id/${id}</a>
              </@key>
            </div>

          </div>

          <#if archiveContacts?? && archiveContacts?has_content>
            <div class="col2">
              <p><strong>For further information about the ${title}, please contact</strong></p>
              <#list archiveContacts as contact>
                <div class="responsibleParty">
                  <@func.displayContact contact, true, true />
                </div>
              </#list>
            </div>
          </#if>

      </div>
    </div>

  </div>
</@skeleton.master>


<#--
Macros
-->
<#macro basicRow classes="">
  <div class="row ${classes}">
    <#nested>
  </div>
</#macro>

<#macro key key keyWidth=3 >
  <#assign valueWidth = 12-keyWidth>
  <@basicRow "key-value">
    <div class="col-sm-${keyWidth} key key-title">${key}</div>
    <div class="col-sm-${valueWidth} value"><#nested></div>
  </@basicRow>
</#macro>

<#macro linebreaks value>
  <#if value?has_content>
    <#list value?split("\n") as line>
        ${line}<#sep><br>
    </#list>
  </#if>
</#macro>

<#macro tags tags>
  <#list tags as tag>
    <#if tag.uri?? && tag.uri?has_content>
      <a href="${tag.uri}">
        <#if tag.value?? && tag.value?has_content>
        ${tag.value}
        <#else>
          ${tag.uri}
        </#if>
      </a>
    <#elseif tag.value?? && tag.value?has_content>
      ${tag.value}
    <#else>
    </#if>
    <#sep>,</#sep>
  </#list>
</#macro>

