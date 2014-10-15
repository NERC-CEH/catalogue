<#if topicCategories?has_content>
<tr>
  <th scope="row">Topic Categories</th>
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
      <#if (descriptiveKeyword.type)?? && descriptiveKeyword.type?has_content>
        <th scope="row">${descriptiveKeyword.type?cap_first} Keywords</th>
      <#else>
        <th scope="row">Keywords</th>
      </#if>
      <td class="descriptive-keywords">
          <#list descriptiveKeyword.keywords as keyword>
            <span property="dc:subject">${keyword.value}</span><#if keyword_has_next>, </#if>
          </#list>
      </td>
    </tr>
</#list>
</#if>
