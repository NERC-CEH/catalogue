<#if responsibleParties?has_content>
  <h2>Further contacts</h2>
  <div id="document-contacts">
    <#list responsibleParties?sort_by("role") as contact>
      <#if contact.role == "Distributor" || contact.role == "Resource Provider">
        <#assign roleProperty> property="dc:publisher"</#assign>
      <#elseif contact.role == "Author">
        <#assign roleProperty> property="dc:creator"</#assign>
      <#else>
        <#assign roleProperty></#assign>
      </#if>
      <#if contact_index % 4 == 0>
        <#if contact_index gt 0>
          </div>
        </#if>
        <div class="blocks">
      </#if>
      <div class="block">
        <H3>${contact.role}</h3>
        <div class="contact">
          <div class="contact-text">
            <strong></strong>
            <#if contact.individualName?has_content>
              <span${roleProperty}>${contact.individualName}</span><br />
            </#if>
            <#if contact.organisationName?has_content>
              <#if !contact.individualName?has_content><span${roleProperty}></#if>${contact.organisationName}<#if !contact.individualName?has_content></span></#if><br />
            </#if>
            <a href="mailto:${contact.email}">${contact.email} <span class="external-link"></span></a>
          </div>
        </div>
      </div>
      <#if !contact_has_next>
        </div>
      </#if>
    </#list>
  </div>
</#if>