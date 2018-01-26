<#if topicCategories?? || descriptiveKeywords??>
  <h3>Keywords</h3>
  <dl id="keywords" class="dl-horizontal">

    <#if topicCategories?has_content>
    <dt>Topic categories</dt>
    <dd>
      <#list topicCategories as topic>
        <#if topic.uri?has_content>
          <a href="${topic.uri?html}" target="_blank" rel="noopener">${codes.lookup('topicCategory', topic.value, 'name')!topic.value}</a>
        <#else>
          <span>${codes.lookup('topicCategory', topic.value, 'name')!topic.value}</span>
        </#if>
        <#if topic_has_next><br></#if>
      </#list>
    </dd>
    </#if>

    <#if descriptiveKeywords?has_content>
      <#list descriptiveKeywords?sort_by("type") as descriptiveKeyword>
        <#if descriptiveKeyword.type?has_content>
          <#if descriptiveKeyword.type == 'INSPIRE Theme' ||  descriptiveKeyword.type == 'CEH Topic'>
            <#assign keywordtype = descriptiveKeyword.type >
          <#else>
            <#assign keywordtype = descriptiveKeyword.type + ' keywords' >
          </#if>
        <#else>
            <#assign keywordtype = 'Other keywords' >
        </#if>
        <dt>
          ${keywordtype?cap_first}
        </dt>
        <dd class="descriptive-keywords">
          <#list descriptiveKeyword.keywords as keyword>
            <#if keyword.uri?has_content>
              <a href="${keyword.uri?html}" target="_blank" rel="noopener">${keyword.value?html}</a>
            <#else>
              <span>${keyword.value?html}</span>
            </#if>
            <#if keyword_has_next><br></#if>
          </#list>
        </dd>
      </#list>
    </#if>
  </dl>
</#if>