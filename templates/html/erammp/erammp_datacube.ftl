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

        <#if description?? && description?has_content>
          <div class="description">
            <@renderLinebreaks description/>
          </div>
        </#if>

        <#if dataFormat?? && dataFormat?has_content>
          <div class="dataFormat">
            <@renderLinebreaks dataFormat/>
          </div>
        </#if>

        <#if locations?? && locations?has_content>
          <@simplerow "Location"><@commaList locations /></@simplerow>
        </#if>

        <#if boundingBoxes?? && boundingBoxes?has_content>
          <@simplerow "Coverage">
            <div id="studyarea-map">
              <#list boundingBoxes as extent>
              <span content="${extent.wkt?html}" datatype="geo:wktLiteral"/>
              </#list>
            </div>
          </@simplerow>
        </#if>

        
        <#if provider?? && provider?has_content>
          <@simplerow "provider${(provider?size > 1)?then('s', '')}">
            <#list provider as contact>
              <#noescape>
                <div class="responsibleParty">      
                  ${func.displayContact(contact, false, true)}
                </div>
              </#noescape>
            </#list>
          </@simplerow>
        </#if>
        
        <#if schema??>
          <div class="section section-schema">
            <h2 class="section-heading">Schema</h2>
            <div class="section-content">
            <table class="table table-schema">
            <thead>
              <tr>
                <th>Field</th>
                <th>Data type</th>
                <th>Description</th>
                <th>Constraints</th>
              </tr>
            </thead>
            <tbody>
            <#list schema as schemaItem>
            <tr>
              <td nowrap="nowrap">
                <#if schemaItem.name?? && schemaItem.name?has_content>
                  ${schemaItem.name}
                </#if>
              </td>
              <td nowrap="nowrap">
                <#if schemaItem.type?? && schemaItem.type?has_content>
                    ${schemaItem.type}
                    <#if schemaItem.format?? && schemaItem.format?has_content>
                      <span class="schema-format">(${schemaItem.format})</span>
                    </#if>          
                </#if>
              </td>
              <td>
                <#if schemaItem.description?? && schemaItem.description?has_content>
                  <span class="schema-description">${schemaItem.description}</span>
                <#else>
                  <span class="nodata" />
                </#if>
              </td>
              <td>
                <#if schemaItem.constraints?? && schemaItem.constraints?has_content>
                  <div class="schema-constraints">
                    <#if schemaItem.constraints.minimum?has_content>
                      <div><span>Min value</span><span>${schemaItem.constraints.minimum}</span></div>
                    </#if>
                    <#if schemaItem.constraints.maximum?has_content>
                      <div><span>Max value</span><span>${schemaItem.constraints.maximum}</span></div>
                    </#if>
                    <#if schemaItem.constraints.minLength?has_content>
                      <div><span>Min length</span><span>${schemaItem.constraints.minLength}</span></div>
                    </#if>
                    <#if schemaItem.constraints.maxLength?has_content>
                      <div><span>Max length</span><span>${schemaItem.constraints.maxLength}</span></div>
                    </#if>
                    <#if schemaItem.constraints.unique=true>
                      <div><span>IS UNIQUE</span></div>
                    </#if>
                  </div>
                <#else>
                  <span class="nodata" />
                </#if>
              </td>
            </tr>    
            </#list>
            </tbody></table>
            </div>
          </div>
        </#if>
        
        <#if keywords?? && keywords?has_content>
          <@simplerow "Keywords">
           <#list keywords as keyword>
            <#if keyword.uri?has_content>
              <a href="${keyword.uri}" target="_blank" rel="noopener noreferrer">${keyword.value}</a>
            <#else>
               ${keyword.value}
            </#if>
            <#if keyword_has_next>, </#if>  
           </#list>
          </@simplerow>
        </#if>

        <div id="section-metadata">
          <h2>Metadata</h2>
            <@simplerow "Record ID">${id}</@simplerow>
            <@simplerow "URL">${uri}</@simplerow>
        </div>

      </div>
    </div>

  <div class="clearfix">&nbsp;</div>
  <div id="footer">
      <small class="pull-right">Last updated ${metadataDateTime?datetime.iso?time?string['d MMMM yyyy, HH:mm']}&nbsp;&nbsp;</small>
  </div>
  </#escape>
</@skeleton.master>

<#macro simplerow label>
  <div class="row">
    <div class="col-sm-3 col-xs-12"><div class="view-label">${label}</div></div>
    <div class="col-sm-9 col-xs-12">
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