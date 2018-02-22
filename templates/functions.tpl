<#function filter things name value >
    <#local result = []>
    <#list things as thing>
        <#if thing[name] == value >
            <#local result = result + [thing]>
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

<#function displayContact contact showPostal showOrcid>
  <#local lcontact = "", displayAddress = "", concatAddress="">

  <#if contact.address?has_content>
    <#local displayAddress = displayAddress + '<div class="postalAddress">'>
    <#if contact.address.deliveryPoint?has_content>
      <#local concatAddress = concatAddress + contact.address.deliveryPoint>             
      <#local displayAddress = displayAddress + '<div>' + contact.address.deliveryPoint + '</div>'>
    </#if>
    <#if contact.address.city?has_content>
      <#if concatAddress = ''>
        <#local concatAddress = concatAddress + contact.address.city>
      <#else>
        <#local concatAddress = concatAddress + ', ' + contact.address.city>
      </#if>
      <#local displayAddress = displayAddress + '<div>' + contact.address.city + '</div>'>
    </#if>
    <#if contact.address.administrativeArea?has_content>
      <#if concatAddress = ''>
        <#local concatAddress = concatAddress + contact.address.administrativeArea>
      <#else>
        <#local concatAddress = concatAddress + ', ' + contact.address.administrativeArea>
      </#if>
      <#local displayAddress = displayAddress + '<div>' + contact.address.administrativeArea + '</div>'>
    </#if>
    <#if contact.address.postalCode?has_content>
      <#if concatAddress = ''>
        <#local concatAddress = concatAddress + contact.address.postalCode>
      <#else>
        <#local concatAddress = concatAddress + ' ' + contact.address.postalCode>
      </#if>
      <#local displayAddress = displayAddress + '<div>' + contact.address.postalCode + '</div>'>
    </#if>
    <#if contact.address.country?has_content>
      <#if concatAddress = ''>
        <#local concatAddress = concatAddress + contact.address.country>
      <#else>
        <#local concatAddress = concatAddress + '. ' + contact.address.country>
      </#if>
      <#local displayAddress = displayAddress + '<div>' + contact.address.country + '</div>'>
    </#if>
    <#local displayAddress = displayAddress + '</div>'>
  </#if>
  
  <#if contact.organisationName?has_content>
    <#if contact.organisationName?matches("unaffiliated(|.+)", "i")>
      <#assign orgName = "">
    <#else>
      <#assign orgName = contact.organisationName>
    </#if>

    <#if contact.email?has_content>
      <#assign mailIcon = " <a class='fa fa-envelope-o contactEmail' title='Email this contact' href='mailto:" + contact.email + "?subject=RE: " + title + "'></a>">
    <#else>
      <#assign mailIcon = "">
    </#if>

    <#if contact.individualName?has_content>
      <#local lcontact = "<div class='individualName' title='"+ concatAddress + "'>" + contact.individualName + mailIcon + "</div><div class='organisationName'>" + orgName + "</div>">
    <#else>
      <#local lcontact = "<div class='organisationName' title='"+ concatAddress + "'>" + orgName + mailIcon + "</div>">
    </#if>

    <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$") && showOrcid =  true>
        <#local lcontact = lcontact + "<div class='nameIdentifier'><a href='" + contact.nameIdentifier + "' target='_blank' rel='noopener noreferrer' title='View this authors record on ORCID.org'><img src='/static/img/orcid_16x16.png' alt='ORCID iD icon' title='ORCID iD'> " + contact.nameIdentifier + "</a></div>">
    </#if>
  </#if>

  <#if showPostal = true>
    <#local lcontact = lcontact + displayAddress>
  <#else>
    <#local lcontact = lcontact>
  </#if>
  <#return lcontact>
</#function>


<#function displaySupplemental supplement showName=false>
  <#local lsupplement = "">

  <#if showName = true &&  supplement.name?has_content>
    <#local lsupplement = lsupplement + "<div class='supplemental-name'>" >
        <#if supplement.url?has_content>
          <#local lsupplement = lsupplement + "<a href='" + supplement.url + "' target='_blank' rel='noopener noreferrer' title='" + supplement.url + "'>" + supplement.name + "</a>" >
        <#else>
          <#local lsupplement = lsupplement + supplement.name >
        </#if>
    <#local lsupplement = lsupplement + "</div>" >
  </#if>

  <#if supplement.description?has_content>
    <#local lsupplement = lsupplement + "<div class='supplemental-description'>" + supplement.description + "</div>">
  </#if>

  <#if (showName = false && supplement.url?has_content) || (showName = true && supplement.url?has_content && !supplement.name?has_content)>
    <#local lsupplement = lsupplement + "<div class='supplemental-url'><a href='" + supplement.url + " target='_blank' rel='noopener noreferrer'>" + supplement.url + "</a></div>">
  </#if>

  <#local lsupplement = "<div class='supplemental-item'>" + lsupplement + "</div>">
  <#return lsupplement>
</#function>