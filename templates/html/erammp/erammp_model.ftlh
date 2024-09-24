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

        <#if description?? && description?has_content>
          <div class="description">
            <@b.linebreaks description/>
          </div>
        </#if>

        <#if modelApproach?? && modelApproach?has_content>
            <@simplerow "Modelling approach">
              <@b.linebreaks modelApproach/>
            </@simplerow>
        </#if>

        <#if boundingBoxes?? && boundingBoxes?has_content>
          <@simplerow "Coverage">
            <div id="studyarea-map" class="studyarea-map">
              <#list boundingBoxes as extent>
              <span content="${extent.bounds}" datatype="geoJson"/>
              </#list>
            </div>
          </@simplerow>
        </#if>

        <#if contacts?? && contacts?has_content>
          <@simplerow "Contact${(contacts?size > 1)?then('s', '')}">
            <#list contacts as contact>
                <div class="responsibleParty">
                  <@func.displayContact contact, true, true />
                </div>
            </#list>
          </@simplerow>
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

        <#if sectors?? && sectors?has_content>
          <@simplerow "Sectors"><@commaList sectors /></@simplerow>
        </#if>
        <#if outputFormats?? && outputFormats?has_content>
          <@simplerow "Output formats"><@commaList outputFormats /></@simplerow>
        </#if>
        <#if programmingLanguages?? && programmingLanguages?has_content>
          <@simplerow "Programming languages"><@commaList programmingLanguages /></@simplerow>
        </#if>
        <#if operatingSystems?? && operatingSystems?has_content>
          <@simplerow "Operating systems"><@commaList operatingSystems /></@simplerow>
        </#if>
        <#if timeSteps?? && timeSteps?has_content>
          <@simplerow "time steps"><@commaList timeSteps /></@simplerow>
        </#if>


        <#if inputs?? && inputs?has_content>
        <div class="section section-inputs">
        <h2>Inputs</h2>
           <table class="table table-responsive">
           <thead>
                <tr>
                  <th>Name</th>
                  <th>Definition</th>
                  <th>Unit</th>
                </tr>
            </thead>
            <tbody>
              <#list inputs as input>
                <tr>
                  <td><#if input.externalName?has_content>${input.externalName}</#if></td>
                  <td><#if input.definition?has_content>${input.definition}</#if></td>
                  <td><#if input.unit?has_content>${input.unit}</#if></td>
                </tr>
              </#list>
             </tbody>
            </table>
       </div>
        </#if>

        <#if outputs?? && outputs?has_content>
        <div class="section section-outputs">
        <h2>Outputs</h2>
          <table class="table table-responsive">
           <thead>
                <tr>
                  <th>Name</th>
                  <th>Definition</th>
                  <th>Unit</th>
                </tr>
            </thead>
            <tbody>
            <#list outputs as output>
                <tr>
                  <td><#if output.externalName?has_content>${output.externalName}</#if></td>
                  <td><#if output.definition?has_content>${output.definition}</#if></td>
                  <td><#if output.unit?has_content>${output.unit}</#if></td>
                </tr>
            </#list>
            </tbody>
            </table>
        </div>
        </#if>

        <div id="section-metadata">
          <h2>Metadata</h2>
            <@simplerow "Record ID">${id}</@simplerow>
            <@simplerow "URL">${uri}</@simplerow>
        </div>

      </div>
    </div>

  <div class="clearfix">&nbsp;</div>
  <div id="footer" class="footer">
      <small class="pull-right">Last updated ${metadataDateTime?datetime.iso?time?string['d MMMM yyyy, HH:mm']}&nbsp;&nbsp;</small>
  </div>
</@skeleton.master>

<#macro simplerow label>
  <div class="row">
    <div class="col-sm-3 col-xs-12"><div class="view-label">${label}</div></div>
    <div class="col-sm-9 col-xs-12">
      <#nested>
    </div>
  </div>
</#macro>

<#macro commaList values>
  <#list values>
    <#items as value>
      ${value}<#sep>,
    </#items>
  </#list>
</#macro>
