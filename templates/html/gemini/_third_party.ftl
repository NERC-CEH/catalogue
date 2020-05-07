<div id="document-thirdParty">   
    <p class="panel-title"><i class="fas fa-info-circle"> </i> This is a dataset managed by a third party</p>
    
    <#if distributorContacts?? &&  distributorContacts?has_content >
    <div class="distributors">
        <h3>Distributor<#if distributorContacts?size gt 1>s</#if></h3>
            <#list distributorContacts as distributorContact>
                <div class="distributor">
                <#if distributorContact.individualName??>${distributorContact.individualName}<br></#if>
                <#if distributorContact.organisationName??>${distributorContact.organisationName}<br></#if>
                <#if distributorContact.email??><a href="mailto:${distributorContact.email}">${distributorContact.email}</a></#if>
                </div>
            </#list>
        </div>
    </#if>

    <#if websites?? &&  websites?has_content >
    <div class="websites">
        <h3>Website<#if websites?size gt 1>s</#if></h3>
            <#list websites as website>
                <div class="website">
                    <a href="${website.url}" target="_blank" rel="noopener noreferrer" title="${website.description}">
                    <#if website.name?has_content>
                        ${website.name}
                    <#else>
                        ${website.url}
                    </#if>
                    </a>
                </div>
            </#list>
        </div>
    </#if>


</div>