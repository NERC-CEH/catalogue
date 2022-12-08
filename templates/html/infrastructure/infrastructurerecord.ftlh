<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as func>
<#import "../blocks.ftlh" as blocks>

<#assign rel_all=jena.allRelatedRecords(uri) >

<#if onlineResources??>
<#assign
    weblinks = func.filter(onlineResources, "function", "website")
    images = func.filter(onlineResources, "function", "image")
>
</#if>


<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <div id="metadata" class="metadata infrastructure-metadata">
    <div class="section section-grey">
      <div class="container">
        <#include "../_adminBasic.ftlh">

        <#if title?? && title?has_content>
          <div id="pageTitle">
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
              <@blocks.linebreaksAndLinks description/>
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

            <#if infrastructureCategory??><@displaykey "Category">${infrastructureCategory.description}</@displaykey></#if>
            <#if capabilities??><@displaykey "Capabilities"><@blocks.linebreaksAndLinks capabilities/></@displaykey></#if>
            <#if lifecycle??><@displaykey "Lifecycle"><@blocks.linebreaksAndLinks lifecycle/></@displaykey></#if>
            <#if uniqueness??><@displaykey "Uniqueness"><@blocks.linebreaksAndLinks uniqueness/></@displaykey></#if>
            <#if partners??><@displaykey "Partners"><@blocks.linebreaksAndLinks partners/></@displaykey></#if>
            <#if access??><@displaykey "Access"><@blocks.linebreaksAndLinks access/></@displaykey></#if>
            <#if locationText??><@displaykey "Location"><@blocks.linebreaksAndLinks locationText/></@displaykey></#if>
            <#if userCosts??><@displaykey "User costs"><@blocks.linebreaksAndLinks userCosts/></@displaykey></#if>
            <#if fundingSources??><@displaykey "Funding sources"><@blocks.linebreaksAndLinks fundingSources/></@displaykey></#if>
            <#if userCosts??><@displaykey "User costs"><@blocks.linebreaksAndLinks userCosts/></@displaykey></#if>

            <#if users?? >
                <@displaykey "Users">
                  <dl>
                    <#list users as user>
                      <dd>${user}</dd>
                    </#list>
                  </dl>
                </@displaykey>
            </#if>

            <#if rel_all?has_content && rel_all?size gt 0 >
            
                <@displaykey "Related">
                  <dl>
                  <#list rel_all as item>
                    <dd>
                    <a href="${item.href}">${item.title}</a>
                    </dd>
                  </#list>
                </dl>
                </@displaykey>
            </#if>

            <#if keywords?? >
              <#assign keywordLabel = "Keyword">
              <#if keywords?size gte 2>
                <#assign keywordLabel = "Keywords">
              </#if>
                <@displaykey keywordLabel>
                  <#list keywords as keyword>
                    ${keyword.value}<#sep>, </#sep>
                  </#list>
                </@displaykey>
            </#if>

            <#if infrastructureScale?? ||  (boundingBoxes?? && boundingBoxes?has_content)>           
              <@displaykey "Scale">
                <#if infrastructureScale??>${infrastructureScale}</#if>
              </@displaykey>
              
              <#if boundingBoxes?? && boundingBoxes?has_content >
                  <div id="studyarea-map">
                  <#list boundingBoxes as extent>
                    <span content="${extent.wkt}" datatype="geo:wktLiteral"/>
                  </#list>
                </div>
              </#if>
            </#if>

            <@displaykey "Last updated">${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</@displaykey>

          </div>
          <div class="col2">


            <@displaykey "Contact">
                <#if owners?? >
                  <#list owners as owner>
                    <div class="contact">
                      <div>
                        <#if owner.individualName?has_content>
                          <span class="fas fa-user"></span>
                        <#else>
                          <span class="fas fa-building"></span>
                        </#if>
                      </div>
                      <div>
                        <#if owner.individualName?has_content>
                          <span>${owner.individualName}</span>
                        </#if>
                        <#if owner.organisationName?? && owner.organisationName?has_content>
                          <span>${owner.organisationName}</span>
                        </#if>
                        <#if owner.address?? && owner.address?has_content>
                          <#if owner.address.city?has_content>
                            <span>${owner.address.city}</span>
                          </#if>
                        </#if>
                        <#if owner.email?? && owner.email?has_content>
                          <span><a href="mailto:${owner.email}"><span class="fas fa-xs fa-envelope"></span> ${owner.email}</a></span>
                        </#if>
                      </div>
                    </div>
                  </#list>
                <#else>
                  <p class="text-danger">NO CONTACT IDENTIFIED</p>  
                </#if>
            </@displaykey>

            <#if weblinks?? && weblinks?has_content >
              <#assign linkLabel = "Link">
              <#if weblinks?size gte 2>
                <#assign linkLabel = "Links">
              </#if>
                <@displaykey linkLabel>
                  <#list weblinks as link>
                  <@displayLink link />
                  </#list>
                </@displaykey>
            </#if>

          </div>
      </div>
    </div>
  
  </div>

  <#include "_footer.ftlh">
</@skeleton.master>

<#macro displaykey label>
  <div class="key-value">
    <div>${label}</div>
    <div><#nested></div>
  </div>
</#macro>

<#macro displayLink link>
  <#if link.name?has_content>
    <#local linkDisplay = link.name>
  <#else>
    <#local linkDisplay = link.url>
  </#if>
  <p><a href="${link.url}" target="_blank" rel="noopener noreferrer">${linkDisplay} <i class="fas fa-external-link-alt fa-xs"></i></a></p>
</#macro>