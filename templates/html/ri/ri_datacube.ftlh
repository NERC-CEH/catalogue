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

          <#if infrastructureCategory??><@displaykey infrastructureCategory "Infrastructure category" /></#if>
          <#if capabilities??><@displaykey capabilities "Capabilities" /></#if>
          <#if lifecycle??><@displaykey lifecycle "Lifecycle" /></#if>
          <#if uniqueness??><@displaykey uniqueness "Uniqueness" /></#if>
          <#if partners??><@displaykey partners "Partners" /></#if>
          <#if access??><@displaykey access "Access" /></#if>
          <#if locationText??><@displaykey locationText "Location" /></#if>
          <#if userCosts??><@displaykey userCosts "User costs"/></#if>
          <#if fundingSources??><@displaykey fundingSources "Funding sources"/></#if>
          <#if scienceArea??><@displaykey scienceArea "Science area" /></#if>

          <#if users?? >
            <#assign numUsers = users?size>
            <div class="key-value">
              <div>User<#if numUsers gte 2>s</#if></div>
              <div><dl>
                <#list users as user>
                  <dd>${user}</dd>
                </#list>
              </dl></div>
            </div>
          </#if>

          <#if onlineResources?? >
           <#assign numLinks = onlineResources?size>
            <div class="key-value">
              <div>Link<#if numLinks gte 2>s</#if></div>
              <div><dl>
                <#list onlineResources as link>
                  <dd>
                  <a href="${link.url}" target="_blank" rel="noopener noreferrer">${link.url} <i class="fas fa-external-link-alt fa-xs"></i></a>
                  </dd>
                </#list>
              </dl></div>
            </div>

          </#if>

          <#if rel_all?has_content && rel_all?size gt 0>
            <div class="relatedRecords">

              <div class="key-value">
                <div>Related records</div>
                <div>
                  <#list rel_all as item>
                    <a href="${item.href}">${item.title}</a><#sep><br></#sep>
                  </#list>
                </div>
              </div>
            </div>
          </#if>

          <#if keywords?? >
            <div class="key-value">
              <div>Keywords</div>
              <div>
                <#list keywords as keyword>
                  ${keyword.value}<#sep>, </#sep>
                </#list>
              </div>
            </div>
          </#if>

          <@displaykey metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm'] "Last updated" />

        
        </div>
        <div class="col2">
          <div class="owner">
            <div>Owner</div>
            <div>
              <#if owners?? >
                <#list owners as owner>
                  <#if owner.individualName??><div>${owner.individualName}</div></#if>
                  <#if owner.organisationName?has_content><div>${owner.organisationName}</div></#if>
                  <#if owner.address?has_content>
                    <#if owner.address.city?has_content><div>${owner.address.city}</div></#if>
                  </#if>
                  <#if owner.email??><a href="mailto:${owner.email}"><div>${owner.email}</a></div></#if>
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

<#macro displaykey key label>
  <div class="key-value">
    <div>${label}</div>
    <div>${key}</div>
  </div>
</#macro>
<#macro linebreaks value>
    <#if value?has_content>
      <#list value?split("\n") as line>
          ${line}<#sep><br>
      </#list>
    </#if>
</#macro>