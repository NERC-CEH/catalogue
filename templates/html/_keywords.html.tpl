<div id="section-keywords">
	<h3><a id="keywords"></a>Keywords</h3>
	<dl class="dl-horizontal">
		<#if topicCategories?has_content>
		<dt>Topic category</dt>
		<#list topicCategories as topic>
		<dd><span property="dct:subject">${topic}</span><#if topic_has_next><br></#if></dd>
		</#list>
		</#if>
		<dt class="placeholder">INSPIRE theme</dt>
		<dd class="placeholder"><a href="http://inspire.ec.europa.eu/theme/ef/" target="_blank" property="dct:subject">Environmental monitoring facilities</a></dd>
		<dt>Other keywords</dt>
		<dd class="truncate">
			<#if descriptiveKeywords?has_content>
			  <#list descriptiveKeywords as descriptiveKeyword>
				  <#list descriptiveKeyword.keywords as keyword>
					<span property="dc:subject">${keyword.value}</span><#if keyword_has_next><br></#if>
				  </#list><#if descriptiveKeyword_has_next><br></#if>
			</#list>
			</#if>
		</dd>
	</dl>
</div>



