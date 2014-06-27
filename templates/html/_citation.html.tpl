<#if otherCitationDetails?has_content>
<div class="panel panel-default">
    <div class="panel-heading"><p class="panel-title">Cite this data</p></div>
    <div class="panel-body">
    	${otherCitationDetails}		  
    </div>
    <div class="panel-footer">
    <#if id?has_content>
	    <a href="http://data.datacite.org/application/x-bibtex/10.5285/${id}">BibTeX</a>
	    <a href="http://data.datacite.org/application/x-research-info-systems/10.5285/${id}">RIS</a>
	</#if>  
    </div>
</div>
</#if>