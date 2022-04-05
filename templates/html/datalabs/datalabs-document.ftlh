<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

  <div id="metadata">
  <div class="container">
    <#include "_admin.ftlh">
    <h1>${title}</h1>
    <#if description?? && description?has_content>
      <div id="description">
        <h3>Description</h3>
        <small>version ${description}</small>
      </div>
    </#if>
    <#if version?? && version?has_content>
      <div id="version">
        <h3>Version</h3></h3>
        <small>${version}</small>
      </div>
    </#if>
    <#if masterUrl?? && masterUrl?has_content>
      <div id="master-url">
        <h3>Master URL</h3>
        <small>${masterUrl}</small>
      </div>
    </#if>
    
    <#if owners?? && owners?has_content>
      <div id="owners">
        <h3>Owners</h3>
        <#list owners as o>
          <div>
            <#if o.individualName??>${o.individualName}</#if>
            <#if o.organisationName??>${o.organisationName}</#if>
            </div>
        </#list>
      </div>
    </#if>
  </div>
</div>

</@skeleton.master>
