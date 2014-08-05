<#if otherCitationDetails?has_content>
<div id="document-citation" class="panel panel-default">
    <div class="panel-heading"><h2 class="panel-title">Cite this data</h2></div>
    <div id="citation" class="panel-body" property="dc:bibliographicCitation">
    	${otherCitationDetails}
    </div>
    <#if id?has_content>
    <div class="panel-footer">
	    <a href="http://data.datacite.org/application/x-bibtex/10.5285/${id}">BibTeX</a>
	    <a href="http://data.datacite.org/application/x-research-info-systems/10.5285/${id}">RIS</a>
    </div>
    </#if>
</div>
</#if>