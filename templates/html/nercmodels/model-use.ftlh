<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as func>
<#import "../blocks.ftlh" as b>
<#import "macros.ftlh" as m>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <@b.metadataContainer "nerc-model nerc-modeluse">

    <@b.admin />

      <div class="model--header">
        <#if title?? && title?has_content>
          <h1>${title}</h1>
        </#if>
      </div>

       <div class="model--main">
          <#if description?? && description?has_content>
            <@m.key "Description">
              <@b.linebreaks description />
            </@m.key>
          </#if>


          <#if (references?? && references?has_content) ||  (onlineResources?? && onlineResources?has_content)>
          <div>
            <h2>References/Online resources</h2>
             
              <#if (references?? && references?has_content)>
                <#list references as link>
                <div>
                  <#if link.name?? && link.name?has_content>${link.name}</#if>
                  <#if link.url?? && link.url?has_content><@m.Url link.url/></#if>
                  <#if link.description?? && link.description?has_content>${link.description}</#if>
                </div>
                </#list>
               </#if>
            
              <#if (onlineResources?? && onlineResources?has_content)>
                <#list onlineResources as link>
                <div>
                  <#if link.name?? && link.name?has_content>${link.name}</#if>
                  <#if link.url?? && link.url?has_content><@m.Url link.url/></#if>
                  <#if link.description?? && link.description?has_content>${link.description}</#if>
                </div>
                </#list>
               </#if>

          </div>
          </#if>

          <#if modelInfos?? && modelInfos?has_content>
            <div>
            <h2>Model Information</h2>
              <#list modelInfos as modelInfo>
                <#assign model=jena.metadata(modelInfo.id)!""/>
                <div class="modelinfo">
                    <div data-toggle="collapse" data-target="#collapse${modelInfo?index}" aria-expanded="false" aria-controls="collapseExample">
                      <a href="${model.href}">${model.title} <i class="fas fa-chevron-down"></i></a>
                    </div>
                    <div class="collapse in" id="collapse${modelInfo?index}">
                      <div class="extra">
                        <#if modelInfo.spatialExtentOfApplication?? && modelInfo.spatialExtentOfApplication?has_content>
                          <div>Spatial extent</div><div>${modelInfo.spatialExtentOfApplication}</div>
                        </#if>
                        <#if modelInfo.availableSpatialData?? && modelInfo.availableSpatialData?has_content>
                          <div>Available spatial data</div><div>${modelInfo.availableSpatialData}</div>
                        </#if>
                        <#if modelInfo.spatialResolutionOfApplication?? && modelInfo.spatialResolutionOfApplication?has_content>
                          <div>Spatial resolution</div><div>${modelInfo.spatialResolutionOfApplication}</div>
                        </#if>
                        <#if (modelInfo.temporalExtentOfApplicationStartDate?? && modelInfo.temporalExtentOfApplicationStartDate?has_content) || (modelInfo.temporalExtentOfApplicationEndDate?? && modelInfo.temporalExtentOfApplicationEndDate?has_content)>
                            <div>Temporal extent</div>
                            <div>
                              <#if modelInfo.temporalExtentOfApplicationStartDate?? && modelInfo.temporalExtentOfApplicationStartDate?has_content>
                                <strong>Start:</strong> ${modelInfo.temporalExtentOfApplicationStartDate}
                              </#if>
                              <#if modelInfo.temporalExtentOfApplicationEndDate?? && modelInfo.temporalExtentOfApplicationEndDate?has_content>
                                <strong>End:</strong> ${modelInfo.temporalExtentOfApplicationEndDate}
                              </#if>
                            </div>
                        </#if>
                        <#if modelInfo.calibrationConditions?? && modelInfo.calibrationConditions?has_content>
                          <div>Calibration</div><div>${modelInfo.calibrationConditions}</div>
                        </#if>
                        <#if modelInfo.notes?? && modelInfo.notes?has_content>
                          <div>Notes</div><div>${modelInfo.notes}</div>
                        </#if>
                      </div>
                    </div>
                </div>
              </#list>
            </div>
          </#if>

          <@m.additionalMetadata />
        </div>
 
 </@b.metadataContainer>
  <#include "_footer.ftlh">
</@skeleton.master>
