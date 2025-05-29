<#assign isEidc = false>
<#if publishers?first.organisationName?matches("(.+?)?Environmental Information Data Centre(.+?)?", "i")>
  <#assign isEidc = true>
</#if>

<div class="alert alert-linked" role="alert">
  <div class="row">
    <#if isEidc>
      <div class="p-2 col-sm-5 col-md-3">
        <img class="img-responsive" src="/static/img/cataloguelogo/eidc_print.png">
      </div>
    </#if>
    <div class="p-2 col-sm-7 col-md-9" >
      <#if isEidc> These data are held and managed by the Environmental Information Data Centre<br></#if>
      <#if masterDocument?? & masterDocument?has_content>
        For more details and to access the data, go to
        <#list masterDocument as item>
          <a href="${item.code}">${item.code}</a><#sep> or </#sep>
        </#list>
      </#if>
    </div>
 </div>
</div>
