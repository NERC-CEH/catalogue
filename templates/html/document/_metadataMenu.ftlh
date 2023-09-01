
<div class="metadataMenu" role="navigation">
    <#if displayMetadataQuality = 1>
        <a class="${alertclass}" href="#section-metadataQuality">
            <i class="fa-solid ${alerticon}"></i>&nbsp;${problemCount} quality issue<#if problemCount gt 1>s</#if>
        </a>
    </#if>
    <a href="#">Description</a>
    <#if displayDataDownload = 1>
        <a href="#download">Access &amp; licensing</a>
    </#if>
    <#if displayProvenance = 1>
        <a href="#prov">Provenance &amp; quality</a>
    </#if>
    <#if displayCitations = 1>
        <a href="#citations">Citations
        <span class="badge badge-citations">${incomingCitationCount}</span>
        </a>
    </#if>
    <#if displaySupplemental = 1>
        <a href="#supplemental">Supplemental information</a>
    </#if>
    <#if displayCorrespondence = 1 || displayAuthors = 1> 
        <a href="#contacts">Contacts/authors</a>
    </#if>
    <#if displayAdditional = 1>
        <a href="#additional">Additional metadata</a>
    </#if>
</div>
