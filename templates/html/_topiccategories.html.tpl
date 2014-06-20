<div>
  <#if topicCategories?has_content>
    <p>Topic category:</p>
    <#list topicCategories as topics>
    	${topics}
    </#list>
  </#if>
</div>