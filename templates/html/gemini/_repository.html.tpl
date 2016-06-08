<#if composedOf?? >
  <#list composedOf>
  <h3>Records</h3>
  <ul class="list-unstyled">
  <#items as record>
    <li><a href="${record.href}">${record.title}</a></li>
  </#items>
  </ul>
  </#list>       
</#if>