<#if otherCitationDetails?has_content><!-- this should be if doi exists -->

	<div id="section-citation">
	  <p>If you reuse this data, you must cite:</p>
	  
		<p id="citation-text" property="dct:bibliographicCitation" about="_:0">
		<#list responsibleParties as author> 		
			<#if author.role == "Author">
				${author.individualName} , <!-- need to sort out trailing comma-->
			</#if>
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