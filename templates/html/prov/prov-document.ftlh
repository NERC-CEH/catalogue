<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as b>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

  <div id="metadata" class="metadata metadata-provDocument ${metadata.state?lower_case}">
    
    <div class="section section-grey">
      <div class="container">
        <#include "../_adminBasic.ftlh">
        <h1>
          ${title}
          <#if (metadata.state == 'draft' || metadata.state == 'pending') >
            <small class="text-danger"><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></small>
          </#if>
        </h1>
        <#if description?? && description?has_content>
          <div><@b.linebreaksAndLinks description /></div>
        </#if>
      </div>
    </div>

    <div class="section">
      <div class="container">

        <#if associations?has_content>
          <div class="associations">
            <#list associations as f>
              <div class="association"><a href="${f.hrefFrom}">${f.titleFrom}</a> - ${f.rel} - <a href="${f.hrefTo}">${f.titleTo}</a></div>
              <#sep><div class="sep"><span class="fa-solid fa-arrow-down-long fa-2x"></span></div></#sep>
            </#list>
          </div>
        </#if>

        <p>Last updated: ${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</p>
      </div>

    </div>

  </div>


</@skeleton.master>