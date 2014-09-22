<#if otherCitationDetails?has_content>

<div class="panel panel-default" id="section-cite">
	<div class="panel-heading"><p class="panel-title">Cite this data</p></div>
	<div class="panel-body" property="dct:bibliographicCitation" about="_:0">
		<div class="zero-clipboard hidden-print">
		<span class="clipboard" data-clipboard-target="citation-text" title="Copy to clipboard">Copy</span>
		</div>
	  <p>If you reuse this data, you must cite:</p>
	  <p id="citation-text">${otherCitationDetails}</p>
	</div>
	<div class="panel-footer hidden-print">
		<div class="btn-group btn-group-xs popover-dismiss hidden-xs" data-placement="left" data-toggle="popover" data-content="Import this citation into your reference management software" data-original-title="" title="">
		<a href="http://data.datacite.org/application/x-bibtex/10.5285/${id}" target="_blank" class="btn btn-default">BibTeX</a>
		<a href="http://data.datacite.org/application/x-research-info-systems/10.5285/${id}" target="_blank" class="btn btn-default">RIS</a>
		</div>                    
	</div>
</div>

</#if>
