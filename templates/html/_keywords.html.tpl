<#if topicCategories?has_content>
<tr>
  <td>Topic Categories</td>
  <td id="topic-categories">
    <#list topicCategories as topic>
      <span property="dc:subject">${topic}</span><#if topic_has_next>, </#if>
    </#list>
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
      <td class="descriptive-keywords">
          <#list descriptiveKeyword.keywords as keyword>
            <span property="dc:subject">${keyword.value}</span><#if keyword_has_next>, </#if>
          </#list>
      </td>
    </tr>
</#list>
</#if>
