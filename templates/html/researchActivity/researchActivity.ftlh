<#import "../skeleton.ftlh" as skeleton>
<#import "../monitoring/monitoringBase.ftlh" as m>

<@base>
  <#assign CombinedGeometriesOutput>
    <@researchActivityCombinedGeometries relCombinedGeometry />
  </#assign>

  <#if CombinedGeometriesOutput?has_content>
    <div class="displayFeatures">
      <div class="featureMap">${CombinedGeometriesOutput}</div>
      <div class="featureList">

        <@activeFacilities relUses  />
        <@pendingFacilities relUses  />
        <@inactiveFacilities relUses  />
       
      </div>
    </div>
  <#else>
    <#if boundingBox?? && boundingBox?has_content>
    <@m.keyValue "Bounding Box" >
      <@m.map boundingBox.bounds />
    </@m.keyValue>
    </#if>
  </#if>

  <div class="relationships">
    <#if !CombinedGeometriesOutput?has_content><@m.relationships relUses "Uses" /></#if>

    <@m.relationships  relSupersedes "Supersedes" />
    <@m.inverseRelationships  relSupersededBy "Superseded by"/>

    <@m.relationships relActivities "Activities set up for this programme" />

    <@m.inverseRelationships relParentResearchActivity "Parent research activities" />
    <@m.relationships relChildResearchActivity "Child research activities" />

    <@m.symmetricRelationships relRelated "Related" />
  </div>
</@base>

<#macro activeFacilities data>
  <#local myData= data?filter(d -> d.availability != "Pending" )?filter(d -> d.availability != "Inactive" ) />
  
  <#if myData?has_content >
    <div>
      <div class="fw-bold border-bottom pb-1 mb-1"><i class="fa-solid fa-circle me-1" style="color:#0000DD;"></i> Facilities (${myData?size})</div>
      <@m.relationships myData "Uses"  />
    </div>
  </#if>
</#macro>

<#macro pendingFacilities data>
  <#local myData= data?filter(d -> d.availability == "Pending" )  />
  
  <#if myData?has_content >
    <div>
      <div class="fw-bold border-bottom pb-1 mb-1"><i class="fa-solid fa-circle me-1" style="color:#cc7a00;"></i> Pending facilities (${myData?size})</div>
      <@m.relationships myData "Uses"  />
    </div>
  </#if>
</#macro>

<#macro inactiveFacilities data>
  <#local myData= data?filter(d -> d.availability == "Inactive" )  />
  
  <#if myData?has_content >
    <div>
      <div class="fw-bold border-bottom pb-1 mb-1"><i class="fa-solid fa-circle me-1" style="color:#999;"></i> Inactive facilities (${myData?size})</div>
      <@m.relationships myData "Uses"  />
    </div>
  </#if>
</#macro>

<#macro researchActivityCombinedGeometries relation>
  <#local combinedGeometry=relation />
  <#if combinedGeometry?has_content>
    <@m.map combinedGeometry />
  </#if>
</#macro>

<#macro base>
  <@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) footer=true>
    <div id="metadata" class="metadata metadata-researchactivity ${metadata.state?lower_case} <#if operationalStatus??>status-${operationalStatus?lower_case}</#if>">
    <#if !type??>
    <#else>
      <section class="section">
        <div class="container">
          <@m.admin />

          <hgroup>
            <#if title?? && title?has_content>
              <h1 class="my-4">
                <div class="fs-5">
                  <#if (metadata.state == 'draft' || metadata.state == 'pending') >
                    <b class="text-danger">${codes.lookup('publication.state', metadata.state)?upper_case!''}</b>
                  </#if>
                  <span class="text-body-tertiary">
                      ${codes.lookup('metadata.activityType', activityType)!'Research activity'}
                  </span>
                </div>
                <div>
                  ${title}  
                </div>

                <@m.alternativeTitles />
              </h1>
            </#if>
          </hgroup>

          <@m.identifiers />

          <#if description?? && description?has_content>
            <@m.keyValue "Description" "">
              <@b.linebreaksAndLinks description />
            </@m.keyValue>
          </#if>

        </div>
      </section>
      <section class="section">
        <div class="container">
          <#nested>

          <#if environmentalDomain??>
           <@m.keyValue "Environmental domain" ""><@m.displayKeywords environmentalDomain ", "/></@m.keyValue>
          </#if>

          <#if purposeOfCollection??>
             <@m.keyValue "Purpose of collection" ""><@m.displayKeywords purposeOfCollection/></@m.keyValue>
          </#if>


          <#if objectives?? && objectives?has_content>
             <@m.keyValue "Objectives" ""><@b.linebreaksAndLinks objectives /></@m.keyValue>
          </#if>

          <#if operatingPeriod?has_content>
            <@m.keyValue "Operating period" "">
              <#list operatingPeriod>
                <#items as op>
                  <@displayPeriod op /><#sep><br></#sep>
                </#items>
               </#list>
            </@m.keyValue>
          </#if>

          <#if contacts?has_content || partners?has_content>
            <div class="contacts">
              <h2>Contacts</h2>
              <#if contacts?has_content>
                <div class="contacts-main">
                  <#list contacts>
                    <#items as contact>
                      <@m.contactDetails contact/>
                    </#items>
                  </#list>
                </div>
              </#if>

              <#if partners?has_content>
                <#list partners>
                  <#items as contact>
                    <@m.contactDetails contact/>
                  </#items>
                </#list>
              </#if>
            </div>
          </#if>

          <#if linksData?has_content>
            <@m.displayLinks linksData "Links to data"/>
          </#if>

          <#if linksOther?has_content>
            <@m.displayLinks linksOther "Other links"/>
          </#if>

          <#if (additionalInfo?? && additionalInfo?has_content) || (keywordsParameters?? && keywordsParameters?has_content) || (keywords?? && keywords?has_content)>
            <div class="pb-5">
              <h2>Additional information</h2>

              <#if additionalInfo?? && additionalInfo?has_content>
                <#list additionalInfo as theInfo>
                  <@m.keyValue theInfo.key?cap_first "">${theInfo.value}</@m.keyValue>
                </#list>
              </#if>

              <#if keywordsParameters?? && keywordsParameters?has_content>
                <@m.keyValue "Parameters measured" ""><@m.displayKeywords keywordsParameters ","/></@m.keyValue>
              </#if>

              <#if keywords?? && keywords?has_content>
                <@m.keyValue "Keywords" ""><@m.displayKeywords keywords ","/></@m.keyValue>
              </#if>
            </div>
          </#if>

          <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
            <#include "../dataResource/_metadataqualityReport.ftlh">
          </#if>

        </div>
      </section>

    </#if>
    </div>

  </@skeleton.master>
</#macro>
