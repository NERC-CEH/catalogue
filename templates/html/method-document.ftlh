<#import "skeleton.ftlh" as skeleton>
<#import "blocks.ftlh" as b>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

  <div id="metadata" class="metadata metadata-methodDocument ${metadata.state?lower_case}">
    
    <div class="section section-grey">
      <div class="container">
       <#include "_adminBasic.ftlh">
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

  </div>


</@skeleton.master>

