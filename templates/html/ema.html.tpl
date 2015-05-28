<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>
  <div id="metadata" class="container" prefix="dc: http://purl.org/dc/terms/ dcat: http://www.w3.org/ns/dcat#">
    
    <#-- TITLE -->
    <#if title?has_content>
      <h1 id="document-title">
      <#if (type)?has_content>
        <small id="resource-type">${codes.lookup('metadata.resourceType', type)!''}</small><br>
      </#if>
      ${title?html}
      <#if (metadata.state == 'draft' || metadata.state == 'pending') >
        <small> - ${codes.lookup('publication.state', metadata.state)!''}</small>
      </#if>
      </h1>
    </#if>

    <#include "metadata/_description.html.tpl">

    <#-- EXTENT -->
    <div id="section-extent">
    <h3 id="extent">Where/When</h3>
        <#if boundingBoxes?has_content || geometry?has_content>
          <div id="studyarea-map">
            <#list boundingBoxes as extent>
              <span content="${extent.wkt?html}" datatype="geo:wktLiteral"/>
            </#list>
            <#if geometry??>
              <span content="${geometry.wkt?html}" datatype="geo:wktLiteral"/>
            </#if>
          </div>
        </#if>
        <#if lifespan?has_content>
          <p>&nbsp;</p>
          <dl>
            <dt>Lifespan</dt>
            <dd id="temporal-extent">
            
              <#if lifespan.start?has_content>
                <span class="extentBegin">${lifespan.start?date}</span>
              <#else>...
              </#if>

              &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;

              <#if lifespan.end?has_content>
                <span class="extentEnd">${lifespan.end?date}</span>
              <#else>present
              </#if>
            </dd>
          </dl>   
        </#if>
    </div>

    <#-- Parameters Measured: These are a special instance of keywords -->
    <#if parametersMeasured?has_content>
      <h3>Parameters Measured</h3>

      <ul class="list-unstyled">
        <#list parametersMeasured as parameter>
          <li>${parameter.title} <#-- HREF for these? --></li>
        </#list>
      </ul>
    </#if>

    <#-- This is the same as parameters measured (pretty much) -->
    <#if keywords?has_content>
      <h3>Keywords</h3>

      <ul class="list-unstyled">
        <#list keywords as keyword>
          <li>${keyword.title} <#-- HREF for these. TARGET=_blank? --></li>
        </#list>
      </ul>
    </#if>

    <#-- responsibleParty  BEGIN -->
    <#if responsibleParties?has_content>
  <div id="document-responsibleParties">
  <h3><a id="responsibleParties"></a>Responsible Parties</h3>
  <dl class="dl-horizontal">

  <#if responsibleParties?has_content>
    <#list responsibleParties as rp>
      <dt>${rp.role.title!rp.role.value?html}</dt>
      <dd>

      
        
          <#if rp.individualName?has_content>
            <span>${rp.individualName?html}</span><br>
          </#if>
          <#if rp.organisationName?has_content>
            <span>${rp.organisationName?html}</span><br>
          </#if>
                
        <#if rp.postalAddress?has_content>
          <address class="hidden-xs">
            <#if rp.postalAddress.street?has_content>${rp.postalAddress.street?html}<br></#if>
            <#list rp.postalAddress.postalArea as area >
              ${area}<br>
            </#list>
            <#if rp.postalAddress.administrativeArea?has_content>${rp.postalAddress.administrativeArea?html}<br></#if>
            <#if rp.postalAddress.postcode?has_content>${rp.postalAddress.postcode?html}<br></#if>
            <#if rp.postalAddress.country?has_content>${rp.postalAddress.country?html}</#if>
          </address>
        </#if>

        <#if rp.email?has_content>
          <address><a href="mailto:${rp.email}">${rp.email}</a></address>
        </#if>

        <#if rp.onlineResource?has_content>
          <address><a href="${rp.onlineResource.href}" target="_blank">${rp.onlineResource.href}</a></address>
        </#if>
      </dd>
    </#list>
  </#if>
  </dl>
  </div>
</#if>  

    <#-- Identifiers -->
    <h3>Identifiers</h3>
    <ul class="list-unstyled">
      <li>${uri}</li>
      <#list identifiers as identifier>
        <li>${identifier.namespace!''}:${identifier.localIdentifier!''}</li>
      </#list>
    </ul>

    <#-- Online Resources -->
    <h3>Online Resources</h3>
      <#-- Href to the online resources with _blank -->
  </div>
  <div id="footer">
      <#include "ef/_footer.html.tpl">
  </div>
</@skeleton.master>
