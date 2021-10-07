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
        <a href="${keyword.uri}" target="_blank" rel="noopener noreferrer">${keyword.value?trim}</a><#if keyword_has_next>,&nbsp;</#if>
    <#else>
        ${keyword.value?trim}<#if keyword_has_next>,&nbsp;</#if>
    </#if>
    </span>
        </#list>
    </dd>
</#if>
