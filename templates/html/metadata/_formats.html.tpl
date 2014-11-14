<#if distributionFormats?has_content>
  <div>
    <p><b>Data are available in these formats:</b></p>
    <ul>
      <#list distributionFormats as format>
        <li property="dc:format">${format.name?html}</li>
      </#list>
    </ul>
  </div>
</#if>
