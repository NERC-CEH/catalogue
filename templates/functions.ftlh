<#function recordTypeOrResourceType>
    <#if metadata.catalogue == "eidc">
        <#return codes.lookup('metadata.recordType', resourceType.value)?lower_case>
    <#else>
        <#return codes.lookup('metadata.resourceType', resourceType.value)?lower_case>
    </#if>
</#function>

<#function filter things name value negate=false>
    <#local result = []>
    <#list things as thing>
        <#if negate=true >
            <#if thing[name] != value >
                <#local result = result + [thing]>
            </#if>
        <#else>
            <#if thing[name] == value >
                <#local result = result + [thing]>
            </#if>
        </#if>
    </#list>
    <#return result>
</#function>

<#function filterRegex things name regex >
    <#local result = []>
    <#list things as thing>
        <#if thing[name]?starts_with(regex) >
            <#local result = result + [thing]>
        </#if>
    </#list>
    <#return result>
</#function>

<#function concatAddress address>
    <#local concatAddress="">

    <#if address.deliveryPoint?has_content>
        <#local concatAddress += address.deliveryPoint>
    </#if>
    <#if address.city?has_content>
        <#if concatAddress?has_content>
            <#local concatAddress += ", ${address.city}">
        <#else>
            <#local concatAddress = address.city>
        </#if>
    </#if>
    <#if address.administrativeArea?has_content>
        <#if concatAddress?has_content>
            <#local concatAddress += ", ${address.administrativeArea}">
        <#else>
            <#local concatAddress = address.administrativeArea>
        </#if>
    </#if>
    <#if address.postalCode?has_content>
        <#if concatAddress?has_content>
            <#local concatAddress += ", ${address.postalCode?upper_case}">
        <#else>
            <#local concatAddress = address.postalCode?upper_case>
        </#if>
    </#if>
    <#if address.country?has_content>
        <#if concatAddress?has_content>
            <#local concatAddress += ", ${address.country?upper_case}">
        <#else>
            <#local concatAddress = address.country?upper_case>
        </#if>
    </#if>
    <#return concatAddress>
</#function>

<#macro displayContact contact showPostal=false showEmail=false showOrcid=false>
    <#if contact.address??>
        <#local address=concatAddress(contact.address)>
    </#if>

    <#if contact.organisationName?has_content>
        <#if contact.organisationName?matches("unaffiliated(|.+)", "i")>
            <#assign orgName = "">
        <#else>
            <#assign orgName = contact.organisationName>
        </#if>

        <#if contact.individualName?has_content>
            <div class="individualName" <#if address??>title="${address}"</#if>>
                ${contact.individualName}
            </div>
            <div class="organisationName">
                ${orgName}
            </div>
        <#else>
            <div class="organisationName" <#if address??>title="${address}"</#if>>
                ${orgName}
            </div>
        </#if>

        <#if showPostal && contact.address?has_content>
            <div class="postalAddress">
                <#if contact.address.deliveryPoint?has_content>
                    ${contact.address.deliveryPoint}<br>
                </#if>
                <#if contact.address.city?has_content>
                    ${contact.address.city}<br>
                </#if>
                <#if contact.address.administrativeArea?has_content>
                    ${contact.address.administrativeArea}<br>
                </#if>
                <#if contact.address.postalCode?has_content>
                    ${contact.address.postalCode?upper_case}<br>
                </#if>
                <#if contact.address.country?has_content>
                    ${contact.address.country?upper_case}<br>
                </#if>
            </div>
        </#if>

        <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$") && showOrcid>
            <div class="nameIdentifier">
              <a href="${contact.nameIdentifier}" target="_blank" rel="noopener noreferrer" title="View this authors record on ORCID.org">
                <img src="/static/img/orcid_16x16.png" alt="ORCID iD icon" title="ORCID iD">&nbsp;${contact.nameIdentifier}
              </a>
            </div>
        </#if>
    </#if>

    <#if contact.phone?? && contact.phone?has_content>
        <div class="contactPhone"><i class="fa-solid fa-phone"></i>&nbsp;${contact.phone}</div>
    </#if>

    <#if showEmail && contact.email?has_content >
        <a href="mailto:${contact.email}"><i class="fa-regular fa-envelope contactEmail"></i>&nbsp;${contact.email}</a>
    </#if>

</#macro>

<#macro displaySupplemental supplement showName=false>
    <div class="supplement">
        <#if showName = true &&  supplement.name?has_content>
            <div class="supplement_name">
                <#if supplement.url?has_content>
                    <a href="${supplement.url}" target="_blank" rel="noopener noreferrer" title="${supplement.url}">${supplement.name}</a>
                <#else>
                    ${supplement.name}
                </#if>
            </div>
        </#if>
        <#if supplement.description?has_content>
            <div class="supplement_description">
                ${supplement.description}
            </div>
        </#if>
        <#if (showName = false && supplement.url?has_content) || (showName && supplement.url?has_content && !supplement.name?has_content)>
            <div class="supplement_url">
                <a href="${supplement.url}" target='_blank' rel="noopener noreferrer">${supplement.url}</a>
            </div>
        </#if>
    </div>
</#macro>
