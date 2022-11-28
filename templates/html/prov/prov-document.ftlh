<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as b>

<#assign
  rel_memberOf = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")
  rel_hasMember = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")
  rel_related = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo") + jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo")
  relationships = rel_memberOf + rel_hasMember + rel_related
> 

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
           
          <#if relationships?has_content>
            <h3>Related record<#if relationships?size gt 1>s</#if></h3>
            
            <#if rel_memberOf?has_content>
              <@displayRelations rel_memberOf "This notebook is used in" /> 
            </#if>

            <#if rel_hasMember?has_content>
              <@displayRelations rel_hasMember "This project uses" /> 
            </#if>

            <#if rel_related?has_content>
              <@displayRelations rel_related "Other related records" /> 
            </#if>

          </#if>
          

          
            Last updated: ${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}
      </div>

    </div>

  </div>


</@skeleton.master>

