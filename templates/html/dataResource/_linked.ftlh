<#assign isEidc = false>
<#if publishers?first.organisationName?matches("(.+?)?Environmental Information Data Centre(.+?)?", "i")>
  <#assign isEidc = true>
</#if>

<div class="alert alert-light border text-center" role="alert">
    <#if isEidc>
      <div><img class="img-responsive" src="/static/img/cataloguelogo/eidc.png"></div>
      <div>These data are held and managed by the Environmental Information Data Centre</div>
    </#if>
    <#if masterDocument?? & masterDocument?has_content>
      For more details and to access the data, go to
      <#list masterDocument as item>
        <a href="${item.code}">${item.code}</a><#sep> or </#sep>
      </#list>
    </#if>
</div>
