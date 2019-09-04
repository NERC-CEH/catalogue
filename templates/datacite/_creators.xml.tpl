<#escape x as x?xml>
<#if authors?has_content>
  <creators>
  <#list authors as author>
    <#if author.individualName?has_content>
    <creator>
      <creatorName>${author.individualName}</creatorName>
      <#if author.nameIdentifier?has_content>
        <#if author.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
          <nameIdentifier nameIdentifierScheme="ORCID" schemeURI="https://orcid.org/">${author.nameIdentifier}</nameIdentifier>
        <#elseif author.nameIdentifier?matches("^http://isni.org/\\d{16}$")>
          <nameIdentifier nameIdentifierScheme="ISNI" schemeURI="http://isni.org/">${author.nameIdentifier}</nameIdentifier>
        </#if>
      </#if>
      <#if author.organisationName?has_content>
        <#if author.organisationName = "Centre for Ecology & Hydrology" || author.organisationName = "UK Centre for Ecology & Hydrology">
          <affiliation affiliationIdentifier="https://ror.org/00pggkr55" affiliationIdentifierScheme="ROR" SchemeURI="https://ror.org/">${author.organisationName}</affiliation>
        <#else>
          <affiliation>${author.organisationName}</affiliation>
        </#if>
      </#if>
    </creator>
    </#if>
  </#list>
  </creators>
</#if>
</#escape>