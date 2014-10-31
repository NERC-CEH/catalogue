<#if distributionFormats?has_content>
  <div>
    <p><b>Data are available in these formats:</b></p>
    <ul>
      <#list distributionFormats as format>
      <li>${format.name}</li>
      </#list>
    </ul>
  </div>
</#if>