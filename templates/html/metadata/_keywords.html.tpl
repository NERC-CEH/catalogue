<#if topicCategories?? || descriptiveKeywords??>
  <h3>Keywords</h3>
  <dl id="keywords" class="dl-horizontal">

    <#if topicCategories?has_content>
    <dt>Topic Categories</dt>
    <dd>
      <#list topicCategories as topic>
        <span property="dcat:theme" content="${topic?html}">${topic?html}</span><#if topic_has_next><br></#if>
      </#list>
    </dd>
    </#if>

    <#if descriptiveKeywords?has_content>
      <!--INSPIRE Theme(s)-->
      <#list descriptiveKeywords as descriptiveKeyword>
      <#if descriptiveKeyword.thesaurusName?? && descriptiveKeyword.thesaurusName.title == 'GEMET - INSPIRE themes, version 1.0'>
        <dt>INSPIRE Theme</dt>
        <dd>
          <#list descriptiveKeyword.keywords as keyword>
            <#if keyword.uri?has_content>
              <a href="${keyword.uri?html}" property="dcat:keyword" target="_blank">${keyword.value?html}</a>
            <#else>
              <span property="dcat:keyword">${keyword.value?html}</span>
            </#if>
            <#if keyword_has_next><br></#if>
          </#list>
        </dd>
        </#if>
      </#list>

      <#list descriptiveKeywords?sort_by("type") as descriptiveKeyword>
        <#if descriptiveKeyword.thesaurusName?? && descriptiveKeyword.thesaurusName.title != 'GEMET - INSPIRE themes, version 1.0'>
          <dt>
            <#if (descriptiveKeyword.type)?? && descriptiveKeyword.type?has_content>${descriptiveKeyword.type?cap_first?html}<#else>Other</#if> keywords
          </dt>
          <dd class="descriptive-keywords">
            <#list descriptiveKeyword.keywords as keyword>
              <#if keyword.uri?has_content>
                <a href="${keyword.uri?html}" property="dcat:keyword" target="_blank">${keyword.value?html}</a>
              <#else>
                <span property="dcat:keyword">${keyword.value?html}</span>
              </#if>
              <#if keyword_has_next><br></#if>
            </#list>
          </dd>
        </#if>
      </#list>
    </#if>
  </dl>
</#if>