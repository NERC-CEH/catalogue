<#function recordTypeOrResourceType>
    <#if metadata.catalogue == "eidc">
        <#return codes.lookup('metadata.recordType', resourceType.value)?lower_case>
    <#else>
        <#return codes.lookup('metadata.resourceType', resourceType.value)?lower_case>
    </#if>
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

        <#if contact.fullName?has_content >
            <div class="individualName" <#if address??>title="${address}"</#if>>
              ${contact.fullName}
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

        <#if contact.isOrcid() && showOrcid>
            <div class="nameIdentifier">
              <a href="${contact.nameIdentifier}" target="_blank" rel="noopener noreferrer" title="View this authors record on ORCID.org">
                <i class="fa-brands fa-orcid fa-xl" aria-label="ORCiD" role="img"></i> ${contact.nameIdentifier}
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

<#--
Dismissible site notice, carrying its own alert markup so that a call site is only ever
the message. Displays up to and including its expiry date, and is gone the day after.

  expiry      "yyyy-MM-dd", the last day the banner is shown.
  catalogueId the catalogue being rendered, normally catalogue.id.
  shownOn     catalogue ids to restrict the banner to; empty shows it on all of them.

Both sides of the date comparison are parsed with an explicit format so that both sit at
midnight. A bare ?date on .now would only re-tag the date-time as a date without dropping
the time of day, which would leave today ahead of an expiry of today and so hide the
banner a day early, for all but the first millisecond of its last day.
-->
<#macro expiringBanner expiry catalogueId="" shownOn=[]>
  <#local todaysDate = .now?string("yyyy-MM-dd")?date("yyyy-MM-dd")>
  <#local expiryDate = expiry?date("yyyy-MM-dd")>
  <#local forThisCatalogue = !shownOn?has_content || shownOn?seq_contains(catalogueId)>

  <#if expiryDate gte todaysDate && forThisCatalogue>
    <div class="m-3 alert alert-warning alert-dismissible fade show" role="alert">
      <#nested>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </#if>
</#macro>
