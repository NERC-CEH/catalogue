<#import "../skeleton.ftl" as skeleton>
<#import "/../../functions.tpl" as func>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <#escape x as x?html>
    <div id="metadata" class="metadata erammp-metadata">
      <div class="container">
        <#include "_admin.ftl">
        
        
        <#if title?? && title?has_content>
          <div id="pageTitle">
            <h1>
              ${title}
              <#if version?? && version?has_content><small>version ${version}</small></#if>
            </h1>
          </div>
        </#if>

        <#if condition?? && condition?has_content && condition=="obsolete">
            <div class="alert alert-danger">
              <b><i class="fas fa-exclamation-triangle"></i> PLEASE DO NOT USE THIS DATA - it is obsolete.</b>
            </div>
        </#if>

        <#if description?? && description?has_content>
        <div class="section">
          <div class="section--content">
            <div class="row">
              <div class="col-sm-9 col-xs-12">
                <div class="description">
                  <@renderLinebreaks description/>
                </div>
              </div>

              <div class="col-sm-3 hidden-xs">
              <#if boundingBoxes?? && boundingBoxes?has_content>
                <div id="studyarea-map" title="Spatial coverage of the data">
                <#list boundingBoxes as extent>
                  <span  content="${extent.wkt?html}" datatype="geo:wktLiteral"/>
                </#list>
                </div>
              </#if>
              </div>

            </div>
          </div>
        </div>
        </#if>
   
        <div class="section">
          <div class="section--content">
            <#if provider?? && provider?has_content>
              <@simplerow "Data provider${(provider?size > 1)?then('s', '')}">
                <#list provider as contact>
                  <#noescape>
                    <div class="responsibleParty">      
                      ${func.displayContact(contact, false, true)}
                    </div>
                  </#noescape>
                </#list>
              </@simplerow>
            </#if>
                
            <#if dataFormat?? && dataFormat?has_content>
              <@simplerow "Data format">${dataFormat}</@simplerow>
            </#if>

            <#if dataLocations?? && dataLocations?has_content>
              <@simplerow "Data location">
                <#list dataLocations as dataLocation>
                    <p>
                      <#if dataLocation.name?has_content><span>Name:</span> ${dataLocation.name}<br></#if>
                      <#if dataLocation.purpose?has_content><span>Purpose:</span> ${dataLocation.purpose}<br></#if>
                      <#if dataLocation.fileLocation?has_content><span>Location:</span>
                          <#if dataLocation.fileLocation?matches("^http(|s)://\\S+$")>
                            <a href="${dataLocation.fileLocation}" target="_blank" rel="noopener noreferrer" title="${dataLocation.fileLocation}">
                            <#if dataLocation.fileLocation?matches("^https://nercacuk.sharepoint.com\\S+$")>
                              ERAMMP Sharepoint site
                            <#else>
                              ${dataLocation.fileLocation}
                            </#if>
                            </a>                        
                          <#else>
                            ${dataLocation.fileLocation}
                          </#if>
                      </#if>
                    </p>
                </#list>
              </@simplerow>
            </#if>
            <#if onlineResources?? && onlineResources?has_content>
              <@simplerow "Additional information">
                <#list onlineResources as item>
                  <p>
                    <#if item.url?has_content>
                    <a href="${item.url}" target="_blank" rel="noopener noreferrer" title="${item.url}">
                      <#if item.name?has_content>
                        ${item.name}
                      <#else>
                        ${item.url}
                      </#if>
                    </a>
                    </#if>
                    <#if item.description?has_content><br>${item.description}</#if>
                  </p>
                </#list>
              </@simplerow>
            </#if>
          </div>
         </div>

        <#if schema??>
        <div class="section">
          <div class="section--heading ">
            <h2>Data structure</h2>
          </div>
          <div class="section--content schema">
            <div class="schema--head">
              <div>Field</div>
              <div>Description</div>
              <div>Type</div>
            </div>
            <#list schema as schemaItem>
            <div class="schema--row">
              <#if schemaItem.name?? && schemaItem.name?has_content><div><span>Field:</span><span>${schemaItem.name}</span></div></#if>
              <#if schemaItem.description?? && schemaItem.description?has_content><div><span>Description:</span><span><@renderLinebreaks schemaItem.description/></span></div></#if>
              <#if schemaItem.type?? && schemaItem.type?has_content><div><span>Type:</span><span>${schemaItem.type}</span></div></#if>
            </div>
            </#list>
          </div>
        </div>
        </#if>

        <#if processingSteps??>
          <div class="section section-processing">
            <div class="section--heading">
              <h2>Data processing steps</h2>
            </div>
            <div class="section--content">
              <#list processingSteps as step>
                <p class="processingStep">
                  <span>${step?index + 1}.</span>
                  ${step.step}
                </p>
              </#list>            
            </div>
          </div>
        </#if>


        <div class="section section-metadata">
          <div class="section--heading">
            <h2>Metadata</h2>
          </div>
          <div class="section--content">
            <@simplerow "Record ID">${id}</@simplerow>
            <@simplerow "URL">${uri}</@simplerow>
            <#if keywords?? && keywords?has_content>
              <@simplerow "Keywords">
              <#list keywords as keyword>
                <#if keyword.uri?has_content>
                  <a href="${keyword.uri}" target="_blank" rel="noopener noreferrer">${keyword.value}</a><#sep>,</#sep>
                <#else>
                  ${keyword.value}<#sep>,</#sep>
                </#if>
              </#list>
              </@simplerow>
            </#if>
            <@simplerow "Record last updated">${metadataDateTime?datetime.iso?time?string['d MMMM yyyy, HH:mm']}</@simplerow>
          </div>
        </div>

      </div>
    </div>
  </#escape>
</@skeleton.master>

<#----------MACROS ---------->
<#macro simplerow label>
  <div class="row">
    <div class="col-sm-2 col-xs-12"><div class="view-label">${label}</div></div>
    <div class="col-sm-10 col-xs-12">
      <#nested>
    </div>
  </div>
</#macro>

<#macro renderLinebreaks content>
    ${content?replace("\n", "<br>")}
</#macro>

<#macro commaList values>
  <#list values>
    <#items as value>
      ${value}<#if value_has_next>,</#if>
    </#items>
  </#list>
</#macro>