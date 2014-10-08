<#if otherCitationDetails?has_content> <!-- this should be if doi exists -->
	<div id="section-cite" property="dct:bibliographicCitation" about="_:0">
	<hr>
	  
	 <p id="citation-text"><small>  
	<#list responsibleParties as author> 		
		<#if author.role == "Author">
			${author.individualName} , <!-- need to sort out trailing comma-->
		</#if>
	</#list>
	
	(<span class="placeholder">2014</span>) 
	${title} . NERC-Environmental Information Data Centre doi:10.5285/${id}
	  
	</small></p>
	<hr>
	</div>
</#if>
