<#if topicCategories?has_content>
<tr>
  <td>Topic Categories</td>
  <td id="topic-categories">
    <#list topicCategories as topic><#if topic_index gt 0>,</#if> ${topic}</#list>
  </td>
</tr>
</#if>
<#if descriptiveKeywords?has_content>
  <#list descriptiveKeywords?sort_by("type") as descriptiveKeyword>
    <tr>
      <#if (descriptiveKeyword.type.value)??>
        <td>${descriptiveKeyword.type.value?cap_first} Keywords</td>
      <#else>
        <td>Keywords</td>
      </#if>
      <td id="descriptive-keywords" property="dc:subject">
          <#list descriptiveKeyword.keywords as keyword><#if keyword_index gt 0>,</#if> ${keyword.value}</#list>
      </td>
    </tr>
</#list>
</#if>
