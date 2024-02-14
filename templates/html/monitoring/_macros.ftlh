<#macro dataRow label="">
    <div class="dataRow">
        <div class="datalabel">${label}</div>
        <div><#nested></div>
    </div>
</#macro>

<#macro keyword kw>
  <#compress>
    <#if kw.uri?has_content>
      <a href="${kw.uri}">${kw.value}</a>
    <#else>
      ${kw.value}
    </#if>
  </#compress>
</#macro>

<#macro geometry geometry>
  <div id="studyarea-map">
      <span content="${geometry}" dataType="geoJson"/>
  </div>
</#macro>

<#macro relationships title description relation> <#--UPDATE-->
  <#local links=jena.relationships(uri, relation) />
  <#if links?has_content>
    <@b.key description "">
      <#list links as link>
        <@b.blockUrl link />
      </#list>
    </@b.key>
  </#if>
</#macro>

<#macro inverseRelationships title description relation> <#--UPDATE-->
  <#local links=jena.inverseRelationships(uri, relation) />
  <#if links?has_content>
    <@b.key description  "">
      <#list links as link>
        <@b.blockUrl link />
      </#list>
    </@b.key>
  </#if>
</#macro>
