<#if modelLinks??>
  <#list modelLinks>
  <h3>Models</h3>
  <ul class="list-unstyled">
  <#items as model>
    <li><a href="${model.href}">${model.title}</a></li>
  </#items>
  </ul>
  </#list>
</#if>