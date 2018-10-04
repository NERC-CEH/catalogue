<#if topicCategories?? || descriptiveKeywords?? ||  inspireTheme??>
  <h3>Tags</h3>
  <dl id="keywords" class="dl-horizontal">

    <#if topicCategories?has_content>
    <dt>Topic categories</dt>
    <dd>
      <#list topicCategories as topic>
        <#if topic.uri?has_content>
          <a href="${topic.uri?html}" target="_blank" rel="noopener noreferrer">${codes.lookup('topicCategory', topic.value, 'name')!topic.value}</a> <#if topic_has_next>, </#if>
        <#else>
          <span>${codes.lookup('topicCategory', topic.value, 'name')!topic.value}</span><#if topic_has_next>, </#if>
        </#if>
       
      </#list>
    </dd>
    </#if>

    <#if descriptiveKeywords?has_content>
     <#assign allKeywords= []>
      <#list descriptiveKeywords as descriptiveKeyword>
        <#list descriptiveKeyword.keywords as keyword>
          <#assign allKeywords = allKeywords + [keyword]>
        </#list>
      </#list>
    </#if>
    <#if allKeywords?has_content>
    <dt>Keywords</dt>
    <dd class="descriptive-keywords">
      <#list allKeywords?sort_by("value") as keyword>
        <span>
        <#if keyword.uri?has_content>
          <a href="${keyword.uri?html}" target="_blank" rel="noopener noreferrer">${keyword.value?trim}</a><#if keyword_has_next>,&nbsp;</#if>
        <#else>
          ${keyword.value?trim}<#if keyword_has_next>,&nbsp;</#if>
        </#if>
        </span>
      </#list>
    </dd>
    </#if>

    <#if inspireTheme??>
      <dt>INSPIRE Theme</dt>
      <dd class="descriptive-keywords">
      <#list inspireTheme?sort_by("theme") as keyword>
        <span>
        <#if keyword.uri?has_content>
          <a href="${keyword.uri?html}" target="_blank" rel="noopener noreferrer">${keyword.theme?trim}</a><#if keyword_has_next><br></#if>
        <#else>
          ${keyword.theme?trim}<#if keyword_has_next><br></#if>
        </#if>
        </span>
      </#list>
    </dd>
    </#if>  

  </dl>
</#if>



 
