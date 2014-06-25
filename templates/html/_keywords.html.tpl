<div>
  <h3>Keywords</h3>
	<div>
	  <#if topicCategories?has_content>
	    <p>Topic category:
	      <#list topicCategories as topics>
	        ${topics}
	      </#list></p>
	  </#if>
	</div>
	<div>
	  <#list descriptiveKeywords as keywordsList>
	    <#list keywordsList.keywords as keyword>
	      ${keyword.value}<br>
	    </#list>
	  </#list>
    </div>
</div>