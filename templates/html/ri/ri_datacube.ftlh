<#import "../skeleton.ftlh" as skeleton>

<#assign rel_all=jena.allRelatedRecords(uri) >

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <div id="metadata" class="metadata ris-metadata">
    <div class="metadata-content container">
      <#include "_admin.ftlh">

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
        <div class="description"><@linebreaks description/></div>
      </#if>
      
      <div class="metadata-grid">
        <div class="col1">

          <#if infrastructureCategory??><@displaykey "Infrastructure category">${infrastructureCategory}</@displaykey></#if>
          <#if capabilities??><@displaykey "Capabilities"><@linebreaks capabilities/></@displaykey></#if>
          <#if lifecycle??><@displaykey "Lifecycle"><@linebreaks lifecycle/></@displaykey></#if>
          <#if uniqueness??><@displaykey "Uniqueness"><@linebreaks uniqueness/></@displaykey></#if>
          <#if partners??><@displaykey "Partners"><@linebreaks partners/></@displaykey></#if>
          <#if access??><@displaykey "Access"><@linebreaks access/></@displaykey></#if>
          <#if locationText??><@displaykey "Location"><@linebreaks locationText/></@displaykey></#if>
          <#if userCosts??><@displaykey "User costs"><@linebreaks userCosts/></@displaykey></#if>
          <#if fundingSources??><@displaykey "Funding sources"><@linebreaks fundingSources/></@displaykey></#if>
          <#if userCosts??><@displaykey "User costs"><@linebreaks userCosts/></@displaykey></#if>
          <#if scienceArea??><@displaykey "Science area">${scienceArea}</@displaykey></#if>

          <#if users?? >
              <@displaykey "Users">
                <dl>
                  <#list users as user>
                    <dd>${user}</dd>
                  </#list>
                </dl>
              </@displaykey>
          </#if>

          <#if onlineResources?? >
            <#assign onlineResourceLabel = "Link">
            <#if onlineResources?size gte 2>
              <#assign onlineResourceLabel = "Links">
            </#if>
              <@displaykey onlineResourceLabel>
                <dl>
                <#list onlineResources as link>
                  <dd>
                  <a href="${link.url}" target="_blank" rel="noopener noreferrer">${link.url} <i class="fas fa-external-link-alt fa-xs"></i></a>
                  </dd>
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
            <#if users?size gte 2>
              <#assign keywordLabel = "Keywords">
            </#if>
              <@displaykey keywordLabel>
                <#list keywords as keyword>
                  ${keyword.value}<#sep>, </#sep>
                </#list>
              </@displaykey>
          </#if>

          <#if boundingBoxes?? && boundingBoxes?has_content >
            <@displaykey "Location">
              <div id="studyarea-map">
               <#list boundingBoxes as extent>
                <span content="${extent.wkt}" datatype="geo:wktLiteral"/>
              </#list>
             </div>
            </@displaykey>
          </#if>

          <@displaykey "Last updated">${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</@displaykey>

        </div>
        <div class="col2">
          <div class="owners">
            <div>Owner <span class="fas fa-user-tie"></span></div>
            <div>
              <#if owners?? >
                <#list owners as owner>
                    <!--FIX THIS -->
                    <#if owner.individualName?has_content><div>${owner.individualName}</div></#if>
                    <#if owner.organisationName?? && owner.organisationName?has_content><div><strong>${owner.organisationName}</strong></div></#if>
                    <#if owner.address?? && owner.address?has_content>
                      <#if owner.address.city?has_content><div>${owner.address.city}</div></#if>
                    </#if>
                    <#if owner.email?? && owner.email?has_content><a href="mailto:${owner.email}"><div><span class="fas fa-xs fa-envelope"></span> ${owner.email}</a></div></#if>
                  
                </#list>
              <#else>
                <p class="text-danger">NO OWNER IDENTIFIED</p>  
              </#if>
            </div>
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

<#macro linebreaks value>
    <#if value?has_content>
      <#list value?split("\n") as line>
          ${line}<#sep><br>
      </#list>
    </#if>
</#macro>
