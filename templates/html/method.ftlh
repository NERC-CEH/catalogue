<#import "skeleton.ftlh" as skeleton>
<#import "../functions.ftlh" as func>
<#import "blocks.ftlh" as b>

<#assign rel_all=jena.allRelatedRecords(uri) >

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) footer=true>
  <div id="metadata" class="metadata method-metadata">
    <div class="section section-grey">
      <div class="container">

        <@b.admin />

        <#if title?? && title?has_content>
          <div id="pageTitle">
            <h1><small>
            <#if (metadata.state == 'draft' || metadata.state == 'pending') >
              <b class="text-danger">${codes.lookup('publication.state', metadata.state)?upper_case!''}</b>
            </#if>Method</small><br>
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
    </div>

    <div class="section">
      <div class="container">
        <div class="metadata-grid">
          <div class="col1">

            <@displaykey "Contact">
                <#if contacts?? >
                  <#list contacts as contact>
                    <@displayContact contact/>
                  </#list>
                <#else>
                  <p class="text-danger">NO CONTACT IDENTIFIED</p>
                </#if>
            </@displaykey>

            <#if onlineResources?? && onlineResources?has_content >
              <#assign linkLabel = "Link">
              <#if onlineResources?size gte 2>
                <#assign linkLabel = "Links">
              </#if>
                <@displaykey linkLabel>
                  <#list onlineResources as link>
                  <@displayLink link />
                  </#list>
                </@displaykey>
            </#if>

            <#if additionalInfo?? && additionalInfo?has_content>
              <@displaykey "Additional information">
              <dl class="dl-horizontal">
                <#list additionalInfo as info>
                   <dt><#if info.key?? && info.key?has_content>${info.key}<#else><span class="text-muted">missing</span></#if></dt>
                   <dd><#if info.value?? && info.value?has_content>${info.value}<#else><span class="text-muted">missing</span></#if></dd>
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
                    <#if keyword.value?has_content>
                      ${keyword.value}
                    <#else>
                      <span class="text-muted">Data missing</span>
                    </#if>
                    <#sep>, </#sep>
                  </#list>
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


            <@displaykey "Last updated">${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</@displaykey>

          </div>
          <div class="col2">


          </div>
      </div>
    </div>

  </div>

</@skeleton.master>

<#--macros -->
<#macro displaykey label>
  <div class="key-value">
    <div>${label}</div>
    <div><#nested></div>
  </div>
</#macro>

<#macro displayLink link>
  <p>
    <#if link.url?has_content>
      <#local linkDisplay = link.url>
      <#if link.name?has_content>
        <#local linkDisplay = link.name>
      </#if>
      <a href="${link.url}" target="_blank" rel="noopener noreferrer">
        ${linkDisplay}
      </a>
     <#else>
      <span class="text-muted">Data missing</span>
     </#if>
  </p>
</#macro>

<#macro displayContact contact>
  <div class="contact">
    <#if contact.individualName?has_content>
      <div>${contact.individualName}</div>
    </#if>
    <#if contact.organisationName?? && contact.organisationName?has_content>
      <div>${contact.organisationName}</div>
    </#if>
    <#if contact.address?? && contact.address?has_content>
      <#if contact.address.city?has_content>
        <div>${contact.address.city}</div>
      </#if>
    </#if>
    <#if contact.email?? && contact.email?has_content>
      <div><a href="mailto:${contact.email}"><span class="fa-solid fa-xs fa-envelope"></span> ${contact.email}</a></div>
    </#if>
  </div>
</#macro>
