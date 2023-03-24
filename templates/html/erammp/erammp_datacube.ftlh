<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as func>
<#import "../blocks.ftlh" as b>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <div id="metadata" class="metadata erammp-metadata">
      <div class="container">
        <#include "_admin.ftlh">


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
              <b><i class="fa-solid fa-triangle-exclamation"></i> PLEASE DO NOT USE THIS DATA - it is obsolete.</b>
            </div>
        </#if>

        <#if description?? && description?has_content>
        <div class="section">
          <div class="section-content">
              <div class="description">
                <@b.linebreaks description/>
              </div>
          </div>
        </div>
        </#if>

        <div class="section">
          <div class="section-content">
            <div class="row">
              <div class="col-md-9 col-sm-12">
                  <#if dataFormat?? && dataFormat?has_content>
                    <@simplerow "Data format">${dataFormat}</@simplerow>
                  </#if>

                  <#if dataLocations?? && dataLocations?has_content>
                    <@simplerow "Data location">
                      <#list dataLocations as dataLocation>
                          <div>
                            <#if dataLocation.name?has_content>${dataLocation.name}<br></#if>
                            <#if dataLocation.purpose?has_content>${dataLocation.purpose}<br></#if>
                            <#if dataLocation.fileLocation?has_content><small>${dataLocation.fileLocation}</small></#if>
                          </div>
                          <sep><br></sep>
                      </#list>
                    </@simplerow>
                  </#if>

                  <#if provider?? && provider?has_content>
                    <@simplerow "Data provider${(provider?size > 1)?then('s', '')}">
                      <#list provider as contact>
                        <div class="responsibleParty">
                            <@func.displayContact contact, false, true />
                        </div>
                      </#list>
                    </@simplerow>
                  </#if>
              </div>
              <div class="col-md-3 col-sm-12">
              <#if boundingBoxes?? && boundingBoxes?has_content>
                <div id="studyarea-map" title="Spatial coverage of the data">
                <#list boundingBoxes as extent>
                  <span  content="${extent.wkt}" datatype="geo:wktLiteral"/>
                </#list>
                </div>
              </#if>
              </div>
            </div>
         </div>
        </div>

        <#if schema??>
          <div class="section section-schema">
            <div class="section-heading">
              <h2>Schema</h2>
            </div>
            <div class="section-content">
            <table class="table table-schema">
            <thead>
              <tr>
                <th>Field</th>
                <th>Description</th>
                <th>Data type</th>
                <th>Constraints</th>
              </tr>
            </thead>
            <tbody>
            <#list schema as schemaItem>
            <#assign rowClass="">
            <#if schemaItem.constraints?? && schemaItem.constraints.unique=true>
              <#assign rowClass="unique">
            </#if>
            <tr class="${rowClass}">
              <td nowrap="nowrap">
                <#if schemaItem.name?? && schemaItem.name?has_content>
                  ${schemaItem.name}
                </#if>
              </td>
              <td>
                <#if schemaItem.description?? && schemaItem.description?has_content>
                  <span class="schema-description">${schemaItem.description}</span>
                <#else>
                  <span class="nodata" />
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

        <#if processingSteps??>
          <div class="section section-schema">
            <div class="section-heading">
              <h2><a data-toggle="collapse" href="#steps-collapse" aria-expanded="false" aria-controls="steps-collapse">Data processing steps <span class="caret" ></span></a></h2>
            </div>
            <div class="section-content collapse" id="steps-collapse">
              <#list processingSteps as step>
                <p class="processingStep">
                  <span>${step?index + 1}.</span>
                  ${step.step}
                </p>
              </#list>
          </div>
        </#if>


        <div class="section section-metadata">
          <div class="section-heading">
            <h2>Metadata</h2>
          </div>
          <div class="section-content">
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

<#macro commaList values>
  <#list values>
    <#items as value>
      ${value}<#if value_has_next>,</#if>
    </#items>
  </#list>
</#macro>
