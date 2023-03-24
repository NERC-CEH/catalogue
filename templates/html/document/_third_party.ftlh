<#if onlineResources??>
    <#assign distributionURLs = func.filter(onlineResources, "function", "browseGraphic", true)>
</#if>
<#if distributorContacts??>
    <#assign dataDistributors = func.filter(distributorContacts, "role", "distributor") >
</#if>

<div id="document-thirdParty">
    <div class="panel panel-default hidden-print" id="document-distribution">
        <div class="panel-body">
            <#assign distributor = "a third party">
            <#if dataDistributors?has_content>
                <#if dataDistributors?first.organisationName?has_content>
                    <#assign distributor = dataDistributors?first.organisationName>
                </#if>

                <#if dataDistributors?first.email?has_content>
                    <#assign distributor = " <a href='mailto:" + dataDistributors?first.email + "' title='" + dataDistributors?first.email + "'>" + distributor + "</a>" >
                </#if>
            </#if>
            <p class="panel-title"> <i class="fa-solid fa-circle-info"> </i> This is a dataset managed by ${distributor}</p>

            <#if distributionURLs?has_content>
                <div class="signpostURL">To access the data visit <a href="${distributionURLs?first.url}">${distributionURLs?first.url}</a></div>
            </#if>


            <#if licences?has_content>
                <div class="licenceText">
                    <div class="divider"></div>
                    <#list licences as licence>
                        <#if licence.code == 'license'>
                        <p>
                            <#if licence.uri?has_content><a rel="license" href="${licence.uri}"></#if>
                                ${licence.value?replace("resource",recordType)}
                                <#if licence.value?contains("Open Government Licence")>
                                    <img class="ogl-logo" src='/static/img/ogl_16.png' alt='OGL'>
                                </#if>
                            <#if licence.uri?has_content></a></#if>
                        </p>
                        </#if>
                    </#list>
                </div>
            </#if>

            <#if otherConstraints?has_content>
                <div class="otherConstraints">
                    <div class="divider"></div>
                    <#list otherConstraints as otherUseConstraint>
                        <p class="otherUseConstraint">
                        <#if otherUseConstraint.uri?has_content>
                            <a href="${otherUseConstraint.uri}">${otherUseConstraint.value}</a>
                        <#else>
                            ${otherUseConstraint.value}
                        </#if>
                        </p>
                    </#list>
                </div>
            </#if>
        </div>
    </div>


    <#if websites?? &&  websites?has_content >
    <div class="websites">
        <h2>Website<#if websites?size gt 1>s</#if></h2>
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
