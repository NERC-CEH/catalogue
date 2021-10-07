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