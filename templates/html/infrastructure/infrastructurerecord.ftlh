<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as func>
<#import "../blocks.ftlh" as b>

<#assign rel_all=jena.allRelatedRecords(uri) >

<#if onlineResources??>
<#assign
    weblinks = func.filter(onlineResources, "function", "website")
    images = func.filter(onlineResources, "function", "image")
>
</#if>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) footer=true>
  <div id="metadata" class="metadata infrastructure-metadata">
    <section class="section section-grey">
      <div class="container">
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
          <div class="description">
            <div class="description-text">
              <@b.linebreaksAndLinks description/>
            </div>
            <div class="browseimage">
              <#if images?? && images?has_content>
                <img src="${images?first.url}" alt="${images?first.name}"/>
              </#if>
            </div>
          </div>
        </#if>

      </div>
    </section>

    <section class="section">
      <div class="container">
        <div class="metadata-grid">
          <div class="col1">

            <#if infrastructureCategory??><@displaykey "Category">${infrastructureCategory.description}</@displaykey></#if>
            <#if capabilities??><@displaykey "Capabilities"><@b.linebreaksAndLinks capabilities/></@displaykey></#if>
            <#if lifecycle??><@displaykey "Lifecycle"><@b.linebreaksAndLinks lifecycle/></@displaykey></#if>
            <#if uniqueness??><@displaykey "Uniqueness"><@b.linebreaksAndLinks uniqueness/></@displaykey></#if>
            <#if partners??><@displaykey "Partners"><@b.linebreaksAndLinks partners/></@displaykey></#if>
            <#if access??><@displaykey "Access"><@b.linebreaksAndLinks access/></@displaykey></#if>
            <#if locationText??><@displaykey "Location"><@b.linebreaksAndLinks locationText/></@displaykey></#if>
            <#if userCosts??><@displaykey "User costs"><@b.linebreaksAndLinks userCosts/></@displaykey></#if>
            <#if fundingSources??><@displaykey "Funding sources"><@b.linebreaksAndLinks fundingSources/></@displaykey></#if>
            <#if userCosts??><@displaykey "User costs"><@b.linebreaksAndLinks userCosts/></@displaykey></#if>

            <#if users?? >
                <@displaykey "Users">
                  <#list users as user>
                    <div>${user}</div>
                  </#list>
                </@displaykey>
            </#if>

            <#if rel_all?has_content && rel_all?size gt 0 >
                <@displaykey "Related">
                  <#list rel_all as item>
                    <div>
                    <a href="${item.href}">${item.title}</a>
                    </div>
                  </#list>
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

            <#if infrastructureScale?? || geometry?? >

              <#if infrastructureScale??>
                <@displaykey "Scale">${infrastructureScale}</@displaykey>
              </#if>

              <#if geometry?? >
                <@displaykey "Location">
                  <div id="studyarea-map" class="studyarea-map">
                    <span content="${geometry.geometryString}" dataType="geoJson"/>
                  </div>
                </@displaykey>
              </#if>
            </#if>
          </div>
          <div class="col2">

            <@displaykey "Contact">
              <#if owners?? >
                <#list owners as owner>
                  <div class="mb-3">
                      <#if owner.fullName?has_content>
                        ${owner.fullName}<br>
                      </#if>
                      <#if owner.organisationName?? && owner.organisationName?has_content>
                        ${owner.organisationName}<br>
                      </#if>
                      <#if owner.address?? && owner.address?has_content>
                        <#if owner.address.city?has_content>
                          ${owner.address.city}<br>
                        </#if>
                      </#if>
                      <#if owner.email?? && owner.email?has_content>
                        <a href="mailto:${owner.email}"><span class="fa-solid fa-xs fa-envelope"></span> ${owner.email}</a>
                      </#if>
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
    </section>

  </div>

</@skeleton.master>

<#--macros -->
<#macro displaykey label>
  <div class="row key-value">
    <div class="col-md-3 key">${label}</div>
    <div class="col-md-9 value"><#nested></div>
  </div>
</#macro>

<#macro displayLink link>
  <#if link.name?has_content>
    <#local linkDisplay = link.name>
  <#else>
    <#local linkDisplay = link.url>
  </#if>
  <p><a href="${link.url}" target="_blank" rel="noopener noreferrer">${linkDisplay} <i class="fa-solid fa-up-right-from-square fa-xs"></i></a></p>
</#macro>
