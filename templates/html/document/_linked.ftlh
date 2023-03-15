<#assign isEidc = "false">
<#if publishers?first.organisationName?matches("(.+?)?Environmental Information Data Centre(.+?)?", "i")>
  <#assign isEidc = "true">
</#if>

<div class="alert--linked" role="alert">
  <#if isEidc = "true">
  <div class="alert--linked-eidc">
    <img class="img-responsive" src="/static/img/logo/eidc.png">
    <div>These data are held and managed by the Environmental Information Data Centre</div>
  </div>
  </#if>
  <#if masterDocument?? & masterDocument?has_content>
    <div>For more details and to access the data, go to
    <#list masterDocument as item>
      <a href="${item.code}">${item.code}</a><#sep> or </#sep>
    </#list>
    </div>
  </#if>
</div>
