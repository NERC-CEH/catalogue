<#if temporalExtent?has_content>
<tr>
  <th scope="row">Temporal Extent</th>
  <td id="temporal-extent" property="dc:temporal" typeof="dc:PeriodOfTime">
    <#list temporalExtent as extent>
      <#if extent_index gt 0>, </#if>
      <#setting date_format = 'yyyy-MM-dd'>
      <#if extent.begin?has_content>
        <span property="dc:start" class="extentBegin">${extent.begin?date}</span>
        <#else>...
      </#if>
      to
      <#if extent.end?has_content>
        <span property="dc:end" class="extentEnd">${extent.end?date}</span>
      <#elseif resourceStatus?has_content && resourceStatus == "onGoing">present
      <#else>...
      </#if>
    </#list>
  </td>
</tr>
</#if>
