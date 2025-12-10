<#assign isEidc = false>
<#if publishers?first.organisationName?matches("(.+?)?Environmental Information Data Centre(.+?)?", "i")>
  <#assign isEidc = true>
</#if>

<div class="alert alert-linked" role="alert">

  <div class="d-flex justify-content-evenly  flex-wrap ">
    <#if isEidc>
      <div class="flex-grow-0 flex-shrink-0 p-2 align-content-center" >
        <img  src="/static/img/cataloguelogo/UKCEH_EIDC_black.png">
      </div>
    </#if>

    <div class="flex-grow-1 flex-shrink-1 p-2 align-content-center">
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
