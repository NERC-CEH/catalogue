<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as func>
<#import "../blocks.ftlh" as b>
<#import "macros.ftlh" as m>

<#if (onlineResources?? && onlineResources?has_content)>
  <#assign websites = func.filter(onlineResources, "function", "website") >
</#if>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) footer=true>

 <div id="metadata" class= "metadata nerc-model nerc-modeluse">
    <div class="section section-grey">
      <div class="container">

        <#if title?? && title?has_content>
          <div id="pageTitle">
            <@b.admin/>
            <h1>
            <#if (metadata.state == 'draft' || metadata.state == 'pending') >
              <small class="text-danger"><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></small>
            </#if>
            ${title}
            </h1>
          </div>
        </#if>

        <#if description??>
          <div class="description">
            <div class="description-text">
              <@b.linebreaks description />
            </div>
            <div class="browseimage">
              <#if images?? && images?has_content>
                <img src="${images?first.url}" alt="${images?first.name}"/>
              </#if>
            </div>
          </div>
        </#if>

      </div>
    </div>

    <div class="section">
      <div class="container">
        <div class="metadata-grid">
          <div class="col1">
            <#if modelInfos?? && modelInfos?has_content>
              <h2>Model Information</h2>
              <div class="models">
                <#list modelInfos as modelInfo>
                  <#assign model=jena.metadata(modelInfo.id)!""/>
                  <div class="models--modelinfo">
                    <div class="models--modelinfo--title"><a href="${model.href}">${model.title}</a></div>
                      <#if
                      (modelInfo.spatialExtentOfApplication?? && modelInfo.spatialExtentOfApplication?has_content )
                      || (modelInfo.availableSpatialData?? && modelInfo.availableSpatialData?has_content)
                      || (modelInfo.spatialResolutionOfApplication?? && modelInfo.spatialResolutionOfApplication?has_content)
                      || (modelInfo.temporalExtentOfApplicationStartDate?? && modelInfo.temporalExtentOfApplicationStartDate?has_content)
                      || (modelInfo.temporalExtentOfApplicationEndDate?? && modelInfo.temporalExtentOfApplicationEndDate?has_content)
                      || (modelInfo.temporalExtentOfApplicationStartDate?? && modelInfo.temporalExtentOfApplicationStartDate?has_content)
                      || (modelInfo.temporalExtentOfApplicationEndDate?? && modelInfo.temporalExtentOfApplicationEndDate?has_content)
                      || (modelInfo.calibrationConditions?? && modelInfo.calibrationConditions?has_content)
                      || (modelInfo.notes?? && modelInfo.notes?has_content)
                      >
                      <div class="models--modelinfo--extra">
                          <#if modelInfo.spatialExtentOfApplication?? && modelInfo.spatialExtentOfApplication?has_content>
                            <div>Spatial extent</div>
                            <div>${modelInfo.spatialExtentOfApplication}</div>
                          </#if>
                          <#if modelInfo.availableSpatialData?? && modelInfo.availableSpatialData?has_content>
                            <div>Available spatial data</div>
                            <div>${modelInfo.availableSpatialData}</div>
                          </#if>
                          <#if modelInfo.spatialResolutionOfApplication?? && modelInfo.spatialResolutionOfApplication?has_content>
                            <div>Spatial resolution</div>
                            <div>${modelInfo.spatialResolutionOfApplication}</div>
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
                            <div>Calibration</div>
                            <div>${modelInfo.calibrationConditions}</div>
                          </#if>
                          <#if modelInfo.notes?? && modelInfo.notes?has_content>
                            <div>Notes</div>
                            <div>${modelInfo.notes}</div>
                          </#if>
                      </div>
                    </#if>
                  </div>
                </#list>
              </div>
            </#if>

            <#if inputData?? && inputData?has_content>
                <@m.key "Input data">
                  <#list inputData as d>
                    ${d.name}<#sep>,</#sep>
                  </#list>
                 </@m.key>
            </#if>
            <#if outputData?? && outputData?has_content>
                <@m.key "Output data">
                  <#list outputData as d>
                    ${d.name}<#sep>,</#sep>
                  </#list>
                 </@m.key>
            </#if>

            <#if additionalInfo?? && additionalInfo?has_content>
              <#list additionalInfo as ai>
                  <@m.key ai.key>
                    ${ai.value}
                  </@m.key>
              </#list>
            </#if>

            <#if (references?? && references?has_content) ||  (websites?? && websites?has_content)>
              <div class="links">
                <h2>References and online resources</h2>

                  <#if (references?? && references?has_content)>
                    <#list references as link>
                    <div class="links--link">
                      <#if link.description?? && link.description?has_content>
                        <div class="links--link--description">${link.description}</div>
                      <#elseif link.name?? && link.name?has_content>
                        <div class="links--link--name">${link.name}</div>
                      <#else>
                        <div>Reference</div>
                      </#if>
                      <#if link.url?? && link.url?has_content><div class="links--link--url"><@m.Url link.url/></div></#if>
                    </div>
                    </#list>
                  </#if>

                  <#if (websites?? && websites?has_content)>
                    <#list websites as link>
                    <div class="links--link">
                      <#if link.description?? && link.description?has_content>
                        <div class="links--link--description">${link.description}</div>
                      <#elseif link.name?? && link.name?has_content>
                        <div class="links--link--name">${link.name}</div>
                      <#else>
                        <div>Website</div>
                      </#if>
                      <#if link.url?? && link.url?has_content><div class="links--link--url"><@m.Url link.url/></div></#if>
                    </div>
                    </#list>
                  </#if>

              </div>
            </#if>

            <@m.additionalMetadata />

          </div>
        </div>
      </div>
    </div>

  </div>

  <#include "_footer.ftlh">
</@skeleton.master>
