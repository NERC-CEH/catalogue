<#import "monitoringBase.ftlh" as m>
<@m.base>

  <#assign CombinedGeometriesOutput>
    <@m.programmeCombinedGeometries relCombinedGeometry />
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

    <@m.inverseRelationships relParentProgramme "Parent programmes" />
    <@m.relationships relChildProgramme "Child programmes" />

    <@m.symmetricRelationships relRelated "Related" />
  </div>
</@m.base>

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