<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as b>
<#import "_macros.ftlh" as m>


<#macro base>
  <@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
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
        </div>
      </div>

      <div class="section">
        <div class="container">

          <#if keywords?? && keywords?has_content>
            <@m.dataRow "Keywords">
              <#list keywords>
                <#items as keyword>
                  <@m.keyword keyword/><#sep>,</#sep>
                </#items>
              </#list>
            </@m.dataRow>
          </#if>

          <@m.dataRow "Last updated">${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</@m.dataRow>
        </div>
      </div>
    </#if>
    </div>
  </@skeleton.master>
</#macro>
