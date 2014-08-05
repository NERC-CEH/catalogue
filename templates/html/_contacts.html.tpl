<#if responsibleParties?has_content>
  <h2>Contacts</h2>
  <div id="document-contacts">
    <table>
      <thead>
        <tr>
          <th>Role</th>
          <th>Name</th>
          <th>Organisation</th>
          <th>Email</th>
          <th>Address</th>
        </tr>
      </thead>
      <tbody>
        <#list responsibleParties?sort_by("role") as contactsList>
          <#if contactsList.role == "Distributor" || contactsList.role == "Resource Provider">
            <#assign roleProperty>property="dc:publisher"</#assign>
          <#elseif contactsList.role == "Author">
            <#assign roleProperty>property="dc:creator"</#assign>
          <#else>
            <#assign roleProperty></#assign>
          </#if>
          <tr ${roleProperty}>
            <td>${contactsList.role}</td>
            <td>${contactsList.individualName}</td>
            <td>${contactsList.organisationName}</td>
            <td><a href="mailto:${contactsList.email}">${contactsList.email}</a></td>
            <td><address>
              <#if contactsList.address.deliveryPoint?has_content>
                ${contactsList.address.deliveryPoint}<br />
              </#if>
              <#if contactsList.address.city?has_content>
                ${contactsList.address.city}<br />
              </#if>
              <#if contactsList.address.administrativeArea?has_content>
                ${contactsList.address.administrativeArea}<br />
              </#if>
              <#if contactsList.address.postalCode?has_content>
                ${contactsList.address.postalCode}<br />
              </#if>
              <#if contactsList.address.country?has_content>
                ${contactsList.address.country}<br />
              </#if>
            </address></td>
          </tr>
        </#list>
      </tbody>
    </table>
  </div>
</#if>