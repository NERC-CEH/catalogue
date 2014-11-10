<h3>Keywords</h3>
<dl id="keywords" class="dl-horizontal">

  <#if topicCategories?has_content>
  <dt>Topic Categories</dt>
    <#list topicCategories as topic>
      <dd>${topic}<#if topic_has_next><br></#if></dd>
    </#list>
  </#if>

  <#if descriptiveKeywords?has_content>
    <!--INSPIRE Theme(s)-->
    <#list descriptiveKeywords as descriptiveKeyword>
    <#if descriptiveKeyword.thesaurusName.title == 'GEMET - INSPIRE themes, version 1.0'>
      <dt>INSPIRE Theme</dt>
      <dd>
        <#list descriptiveKeyword.keywords as keyword>
          <#if keyword.uri??>
            <a href="${keyword.uri}" property="dcat:keyword" target="_blank">${keyword.value}</a>
          <#else>
            <span property="dc:subject">${keyword.value}</span>
          </#if>
          <#if keyword_has_next><br></#if>
        </#list>
      </dd>
      </#if>
    </#list>
    
    <#list descriptiveKeywords?sort_by("type") as descriptiveKeyword>
      <#if descriptiveKeyword.thesaurusName.title != 'GEMET - INSPIRE themes, version 1.0'>
        <dt>
          <#if (descriptiveKeyword.type)?? && descriptiveKeyword.type?has_content>${descriptiveKeyword.type?cap_first}<#else>Other</#if> keywords
        </dt>
        <dd class="descriptive-keywords">
          <#list descriptiveKeyword.keywords as keyword>
            <#if keyword.uri??>
              <a href="${keyword.uri}" property="dcat:keyword" target="_blank">${keyword.value}</a>
            <#else>
              <span property="dcat:keyword">${keyword.value}</span>
            </#if>
            <#if keyword_has_next><br></#if>
          </#list>
        </dd>
      </#if>
    </#list>
  </#if>
</dl>
