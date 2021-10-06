<#if keywords?has_content>
    <dt>Keywords</dt>
    <dd class="descriptive-keywords">
    <#list keywords?sort_by("value") as keyword>
    <span>
    <#if keyword.uri?has_content>
        <a href="${keyword.uri}" target="_blank" rel="noopener noreferrer">${keyword.value?trim}</a><#if keyword_has_next>,&nbsp;</#if>
    <#elseif keyword.value?has_content>
        ${keyword.value?trim}<#if keyword_has_next>,&nbsp;</#if>
    </#if>
    </span>
        </#list>
    </dd>
</#if>