<#import "../../functions.ftlh" as func>

<div class="alert alert-info alert--linked" role="alert">
  <p><i class="fas fa-info-circle"></i> 
  These data are held and managed by the Environmental Information Data Centre, hosted at UKCEH.</p>

  <#if masterDocument?? & masterDocument?has_content>
    <p>For more details and to access the data, go to
    <#list masterDocument as item>
      <a href="${item.code}">${item.code}</a><#sep> or </#sep>
    </#list>
    </p>
  </#if>

</div>
