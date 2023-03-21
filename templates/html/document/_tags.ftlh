<#if topicCategories?has_content>
<dt>Topic categories</dt>
<dd>
  <#list topicCategories as topic>
    <#if topic.uri?has_content>
      <a href="${topic.uri}" target="_blank" rel="noopener noreferrer">${codes.lookup('topicCategory', topic.value, 'name')!topic.value}</a> <#if topic_has_next>, </#if>
    <#else>
      <span>${codes.lookup('topicCategory', topic.value, 'name')!topic.value}</span><#if topic_has_next>, </#if>
    </#if>
    
  </#list>
</dd>
</#if>

<#assign allKeywords= []>
<#if descriptiveKeywords?has_content>
  <#list descriptiveKeywords as descriptiveKeyword>
    <#list descriptiveKeyword.keywords as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
    </#list>
  </#list>
</#if>
<#if keywords?has_content>
  <#list keywords as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
  </#list>
</#if>
<#if allKeywords?has_content>
<dt>Keywords</dt>
<dd class="keywords">
  <#list allKeywords?sort_by("value") as keyword>
    <span>
    <#if keyword.uri?has_content>
      <abbr title="${keyword.uri}">${keyword.value?trim}</abbr><a href="${keyword.uri}" target="_blank" rel="noopener noreferrer"><sup class="fa-solid fa-sm fa-arrow-up-right-from-square"></sup></a><#if keyword_has_next>,&nbsp;</#if>
    <#else>
      ${keyword.value?trim}<#if keyword_has_next>,&nbsp;</#if>
    </#if>
    </span>
  </#list>
</dd>
</#if>

<#if inspireThemes??>
  <dt>INSPIRE Theme</dt>
  <dd >
  <#list inspireThemes?sort_by("theme") as inspireTheme>
    <span>
    <#if inspireTheme.uri?has_content>
      <a href="${inspireTheme.uri}" target="_blank" rel="noopener noreferrer">${inspireTheme.theme?trim}</a><#if inspireTheme_has_next><br></#if>
    <#else>
      ${inspireTheme.theme?trim}<#if inspireTheme_has_next><br></#if>
    </#if>
    </span>
  </#list>
</dd>
</#if>  