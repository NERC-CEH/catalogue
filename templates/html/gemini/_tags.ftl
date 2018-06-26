<#if topicCategories?? || descriptiveKeywords??>
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

    <#--INSPIRE Themes-->
    <#if descriptiveKeywords?has_content>
    <#assign INSPIREthemes = func.filter(descriptiveKeywords, "type", "INSPIRE Theme")>
    <#if INSPIREthemes?has_content>
    <#list INSPIREthemes as themes>
      <dt>INSPIRE Theme</dt>
      <dd class="descriptive-keywords">
      <#list themes.keywords as theme>
          <#if theme.uri?has_content>
            <a href="${theme.uri?html}" target="_blank" rel="noopener noreferrer">${theme.value?trim}</a>
          <#else>
            ${theme.value?trim}
          </#if>
          <#if theme_has_next><br></#if>
      </#list>
      </dd>
    </#list>
    </#if>
    
    <#--MERGE ALL OTHER KEYWORDS INTO A SINGLE LIST-->
    <#assign otherKeywords = func.filter(descriptiveKeywords, "type", "INSPIRE Theme", true)>
     <#assign allKeywords= []>
      <#list otherKeywords as descriptiveKeyword>
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
  </dl>
</#if>