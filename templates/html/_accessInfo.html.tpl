<#list useLimitations as useLimitations>
	<#if useLimitations?starts_with("If you reuse this data")>
	<#else>
	<p>${useLimitations}</p>
	</#if>
</#list>

<#if resourceIdentifiers?has_content>
	<#list resourceIdentifiers as uri>
		<#if uri.codeSpace="doi:" >
		<#if uri.code?length == 44>

			<div id="section-citation">
			  <p>If you reuse this data, you must cite:</p>
			  
				<p id="citation-text" property="dct:bibliographicCitation" about="_:0">
				
				<#list authors as author>
					${author.individualName}<#if author_has_next>,</#if>
				</#list>

					<#if datasetReferenceDate.publicationDate??>
						<#setting date_format = 'yyyy-MM-dd'>
						(${datasetReferenceDate.publicationDate?substring(0, 4)})
					</#if>
				.
				${title}. NERC-Environmental Information Data Centre doi:10.5285/${id}
				</p>
			
				<div class="btn-group btn-group-xs hidden-xs" title="Import this citation into your reference management software">
					<a href="http://data.datacite.org/application/x-bibtex/10.5285/${id}" target="_blank" class="btn btn-default">BibTeX</a>
					<a href="http://data.datacite.org/application/x-research-info-systems/10.5285/${id}" target="_blank" class="btn btn-default">RIS</a>
					<a href="http://data.datacite.org/application/citeproc+json/10.5285/${id}" target="_blank" class="btn btn-default">CSL</a>
				</div>                    
			</div>	

		</#if>
		</#if>
	</#list>
</#if>



	<!--
	May need to add this in but I'd prefer to hide most of this guff.
	
	<dl class="dl-horizontal">
	  
	<#if useLimitations?has_content>
		<dt>Use limitations</dt>
		<dd>
			<#list useLimitations as useLimitations>
			<p>${useLimitations}</p>
			</#list>
		 </dd>
	</#if>

	<#if otherConstraints?has_content>
		<dt>Other constraints</dt>
		<dd>
			<#list otherConstraints as otherConstraints>
			<p>${otherConstraints}</p>
			</#list>
		 </dd>
	</#if>
	
	<#if accessConstraints?has_content>
		<dt>Access constraints</dt>
		<dd>
			<#list accessConstraints as accessConstraints>
			${accessConstraints}<#if accessConstraints_has_next><br></#if>
			</#list>
		 </dd>
	</#if>
	
	<#if securityConstraints?has_content>
		<dt>Security constraints</dt>
		<dd>
			<#list securityConstraints as securityConstraints>
			<p>${securityConstraints}</p>
			</#list>
		 </dd>
	</#if>
  </dl>-->   
