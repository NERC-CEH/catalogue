<#escape x as x?xml>
<contributors>
    <#if pocs?? && pocs?has_content>
    <#list pocs as contact>
        <contributor contributorType="ContactPerson">
        <#if contact.individualName?has_content>
            <contributorName nameType="Personal">${contact.individualName}</contributorName>
            <#if contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
                <nameIdentifier nameIdentifierScheme="ORCID" schemeURI="https://orcid.org/">${contact.nameIdentifier}</nameIdentifier>
            </#if>
            <#if contact.organisationIdentifier?has_content && contact.organisationIdentifier?matches("^https://ror.org/\\w{8,10}$")>
                <affiliation affiliationIdentifier="${contact.organisationIdentifier}" affiliationIdentifierScheme="ROR" SchemeURI="http://ror.org/">${contact.organisationName}</affiliation>
            <#else>
                <affiliation>${contact.organisationName}</affiliation>
            </#if>
        <#else>
            <contributorName nameType="Organizational">${contact.organisationName}</contributorName>
                <#if contact.organisationIdentifier?has_content && contact.organisationIdentifier?matches("^https://ror.org/\\w{8,10}$")>
                <nameIdentifier nameIdentifierScheme="ROR" schemeURI="http://ror.org/">${contact.organisationIdentifier}</nameIdentifier>
                </#if>
        </#if>
        </contributor>
    </#list>
    </#if>
    <#if rightsHolders?? && rightsHolders?has_content>
    <#list rightsHolders as contact>
        <contributor contributorType="RightsHolder">
        <contributorName nameType="Organizational">${contact.organisationName}</contributorName>
        <#if contact.organisationIdentifier?has_content && contact.organisationIdentifier?matches("^https://ror.org/\\w{8,10}$")>
            <nameIdentifier nameIdentifierScheme="ROR" schemeURI="http://ror.org/">${contact.organisationIdentifier}</nameIdentifier>
        </#if>
        </contributor>
    </#list>
    </#if>
    <#if custodians?? && custodians?has_content>
    <#list custodians as contact>
        <contributor contributorType="HostingInstitution">
        <contributorName nameType="Organizational">${contact.organisationName}</contributorName>
        <#if contact.organisationIdentifier?has_content && contact.organisationIdentifier?matches("^https://ror.org/\\w{8,10}$")>
            <nameIdentifier nameIdentifierScheme="ROR" schemeURI="http://ror.org/">${contact.organisationIdentifier}</nameIdentifier>
        </#if>
        </contributor>
    </#list>
    </#if>
</contributors>
</#escape>

