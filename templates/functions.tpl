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

<#function displayContact contact showPostal>
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
    <#if contact.email?has_content>
      <#if contact.individualName?has_content>
        <#local lcontact = lcontact + "<div class='individualName' title='"+ concatAddress + "'><a href='mailto:" + contact.email + "&amp;subject=RE: " + title + "'>" + contact.individualName + "</a></div>">
        <#if contact.organisationName?has_content>
          <#local lcontact = lcontact + "<div class='organisationName'>" + contact.organisationName  + "</div>">
        </#if>
      <#else>
        <#local lcontact = lcontact + "<div class='organisationName' title='"+ concatAddress + "'><a href='mailto:" + contact.email + "&amp;subject=RE: " + title + "'>" + contact.organisationName + "</a></div>">
      </#if>
    <#else>
      <#if contact.individualName?has_content>
        <#local lcontact = lcontact + "<div class='individualName' title='"+ concatAddress + "'>" + contact.individualName + "</div>">
      </#if>
      <#if contact.organisationName?has_content>
        <#local lcontact = lcontact + "<div class='organisationName' title='"+ concatAddress + "'>" + contact.organisationName + "</div>">
      </#if>
    </#if>
  </#if>

  <#if showPostal = true>
    <#local lcontact = lcontact + displayAddress>
  <#else>
    <#local lcontact = lcontact>
  </#if>
  <#return lcontact>
</#function>








