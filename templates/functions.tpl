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

<#function displayContact contact showPostal=false showEmail=false showOrcid=false>
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

    <#if contact.individualName?has_content>
      <#local lcontact = "<div class='individualName' title='"+ concatAddress + "'>" + contact.individualName  + "</div><div class='organisationName'>" + orgName + "</div>">
    <#else>
      <#local lcontact = "<div class='organisationName' title='"+ concatAddress + "'>" + orgName + "</div>">
    </#if>

    <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$") && showOrcid = true>
        <#local lcontact = lcontact + "<div class='nameIdentifier'><a href='" + contact.nameIdentifier + "' target='_blank' rel='noopener noreferrer' title='View this authors record on ORCID.org'><img src='/static/img/orcid_16x16.png' alt='ORCID iD icon' title='ORCID iD'> " + contact.nameIdentifier + "</a></div>">
    </#if>
  </#if>

  <#if showPostal = true>
    <#local lcontact = lcontact + displayAddress>
  <#else>
    <#local lcontact = lcontact>
  </#if>


  <#if contact.phone?? && contact.phone?has_content>
    <#local lcontact = lcontact + "<div class='contactPhone'><i class='fas fa-phone'></i> &nbsp;" + contact.phone + "</div>">
  </#if>


  <#if showEmail=true && (contact.email?has_content || metadata.catalogue = "eidc")>
    <#assign emailSubject= "?subject=" + title>
    <#if contact.email?has_content>
      <#assign emailAddress=contact.email>
    <#else>
      <#assign emailAddress= "enquiries@ceh.ac.uk">
    </#if>
    <#if contact.individualName?has_content>
      <#assign emailSubject = emailSubject + " (FAO: " + contact.individualName + ")">
    </#if>
    <#local lcontact = lcontact + "<a href='mailto:" + emailAddress + emailSubject  + "'><i class='far fa-envelope contactEmail'></i> &nbsp;" +  emailAddress + "</a>">
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
    <#local lsupplement = lsupplement + "<div class='supplemental-url'><a href='" + supplement.url + "' target='_blank' rel='noopener noreferrer'>" + supplement.url + "</a></div>">
  </#if>

  <#local lsupplement = "<div class='supplemental-item'>" + lsupplement + "</div>">
  <#return lsupplement>
</#function>