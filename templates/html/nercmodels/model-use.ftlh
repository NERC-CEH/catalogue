<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as func>
<#import "../blocks.ftlh" as b>
<#import "macros.ftlh" as m>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <@b.metadataContainer "ceh-model">

    <@b.admin />

    <#if title?? && title?has_content>
      <h1>${title}</h1>
    </#if>

    <#if description?? && description?has_content>
      <@m.key "Description">
        <@b.linebreaks description />
      </@m.key>
    </#if>


    <#if references?? && references?has_content>
    <div>
      <h2>References</h2>

      <#list references as ref>
      <div>
        <#if ref.name?? && ref.name?has_content>${ref.name}</#if>
        <#if ref.url?? && ref.url?has_content><@m.Url ref.url true/></#if>
        <#if ref.description?? && ref.description?has_content>${ref.description}</#if>
      </div>
      </#list>
    </div>
    </#if>


    <#if modelInfos?? && modelInfos?has_content>
      <div>
      <h2>Model Information</h2>
        <#list modelInfos as modelInfo>
          <#assign model=jena.metadata(modelInfo.id)!""/>
          <div>
            <a href="${model.href}">${model.title}</a>
             ${modelInfo.notes}
          </div>
        </#list>
      </div>
    </#if>

    <@m.additionalMetadata />
 
 </@b.metadataContainer>
  <#include "_footer.ftlh">
</@skeleton.master>
