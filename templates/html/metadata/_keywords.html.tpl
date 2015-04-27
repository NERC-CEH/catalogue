<#if topicCategories?? || descriptiveKeywords??>
  <h3>Keywords</h3>
  <dl id="keywords" class="dl-horizontal">

    <#if topicCategories?has_content>
    <dt>Topic Categories</dt>
    <dd>
      <#list topicCategories as topic>
        <#if topic.uri?has_content>
          <a href="${topic.uri?html}" target="_blank">${codes.lookup('topicCategory', topic.value)}</a>
        <#else>
          <span>${codes.lookup('topicCategory', topic.value)!topic.value}</span>
        </#if>
        <#if topic_has_next><br></#if>
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
                <a href="${keyword.uri?html}" target="_blank">${keyword.value?html}</a>
              <#else>
                <span>${keyword.value?html}</span>
              </#if>
              <#if keyword_has_next><br></#if>
            </#list>
          </dd>
        </#if>
      </#list>

      <#list descriptiveKeywords?sort_by("type") as descriptiveKeyword>
        <#if (descriptiveKeyword.thesaurusName?? && descriptiveKeyword.thesaurusName.title != 'GEMET - INSPIRE themes, version 1.0') || ( !descriptiveKeyword.thesaurusName??)>
          <dt>
            <#if descriptiveKeyword.type?? && descriptiveKeyword.type?has_content>${descriptiveKeyword.type?cap_first?html}<#else>Other</#if> keywords
          </dt>
          <dd class="descriptive-keywords">
            <#list descriptiveKeyword.keywords as keyword>
              <#if keyword.uri?has_content>
                <a href="${keyword.uri?html}" target="_blank">${keyword.value?html}</a>
              <#else>
                <span>${keyword.value?html}</span>
              </#if>
              <#if keyword_has_next><br></#if>
            </#list>
          </dd>
        </#if>
      </#list>
    </#if>
  </dl>
</#if>