<#assign custodianEIDC = func.filter(func.filter(otherContacts, "role", "custodian"), "organisationName", "Environmental Information Data Centre") >

<#if onlineResources?has_content>
  <#assign orders = 
  func.filterRegex(func.filter(onlineResources, "function", "order"), "url", "^http[s?]://catalogue.ceh.ac.uk/download.+") + func.filterRegex(func.filter(onlineResources, "function", "order"), "url", "^https://order-eidc.ceh.ac.uk/resources/[A-Z0-9a-z]{8}/order")>
</#if>


<#if resourceType?? && permission.userCanEdit(id)>
  <div class="row hidden-print" id="adminPanel">
    <div class="text-right" id="adminToolbar" role="toolbar">
      
      <div class="btn-group btn-group-sm">
        <button type="button" class="btn btn-default btn-wide edit-control"  data-document-type="${metadata.documentType}">Edit</button>
        <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
          <span class="caret"></span>
          <span class="sr-only">Toggle Dropdown</span>
        </button>
        <ul class="dropdown-menu dropdown-menu-right">
          <li><a href="/documents/${id?html}/permission"><i class="fas fa-users"></i> Permissions</a></li>
          <li><a href="/documents/${id?html}/publication"><i class="fas fa-eye"></i> Publication status</a></li>
          <li role="separator" class="divider"></li>
          <li><a href="/documents/${id?html}/catalogue" class="catalogue-control"><i class="fas fa-sign-out-alt"></i> Move to a different catalogue</a></li>
        </ul>
        </#if>
      </div>

      <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
        
      
        <#-- DOI button -->
        <#if metadata.documentType != 'LINK_DOCUMENT'>
          <#if metadata.catalogue == "eidc" && (resourceType.value == 'dataset' | resourceType.value == 'nonGeographicDataset' | resourceType.value == 'application')>

            <#assign menuLabel="DOI">
            <#if dataciteMintable>
              <#assign menuLabel="Mint DOI">
            <#else>
              <#if !datacitable>
                <#assign menuLabel="<i class='fas fa-exclamation-triangle text-red'></i> DOI">
              </#if>
            </#if>
            <div class="btn-group btn-group-sm" role="group">
              <button type="button" class="btn btn-default btn-wide dropdown-toggle" data-toggle="dropdown">${menuLabel} <span class="caret"></span></button>
                <ul class="dropdown-menu dropdown-menu-right">
                  <#if dataciteMintable>
                    <li><div class="msg text-success"><i class="fas fa-check text-success"></i> There is enough information<br>to assign a DOI to this resource</div></li>
                    <#if permission.userCanDatacite()>
                      <form action="/documents/${id?html}/datacite" method="POST" class="mintDOI">
                          <button type="submit" class="btn btn-success" value="Mint DOI">Mint DOI</button>
                      </form>
                    </#if>
                    <li class="divider"></li>
                  <#else>
                    <#if !datacitable >
                      <li><div class="msg text-red"><i class="fas fa-exclamation"></i> Conditions for assigning a <br>DOI have not been met</div></li>
                      <li><a href="https://www.eidc.ac.uk/admin/guidance/mintADOI" target="_blank" rel="noopener noreferrer">Help</a></li>
                    </#if>
                  </#if>
                  <#if datacitable>
                    <li><a href="/documents/${id?html}/datacite.xml">View DataCite XML</a></li>
                  </#if>
                </ul>
            </div>
          </#if>

          <#-- 'More' button -->
          <#if orders?has_content || custodianEIDC?size gte 1 || (metadata.catalogue == "eidc" && metadata.documentType != 'LINK_DOCUMENT' && (resourceType.value == 'dataset' | resourceType.value == 'nonGeographicDataset' | resourceType.value == 'application'))>
          <div class="btn-group btn-group-sm" role="group">
            <button type="button" class="btn btn-default btn-wide dropdown-toggle" data-toggle="dropdown">More <span class="caret"></span></button>
              <ul class="dropdown-menu dropdown-menu-right">
                <#if metadata.catalogue == "eidc" && metadata.documentType != 'LINK_DOCUMENT' && (resourceType.value == 'dataset' | resourceType.value == 'nonGeographicDataset' | resourceType.value == 'application')>
                  <li>
                    <a href="/upload/${id}">Manage data</a>
                  </li>
                </#if>
                <#if orders?has_content>
                  <#list orders as testLink>
                    <#if testLink.function == "order">
                      <li><a href="${testLink.url?html}&test=true">Test order</a></li>
                    </#if>
                  </#list>
                </#if>

                 <#if (metadata.state == 'draft' || custodianEIDC?size gte 1 ) >
                  <li><a href="https://jira.ceh.ac.uk/issues/?jql=cf%5B13250%5D%3D%22${id?html}%22" target="_blank" rel="noopener noreferrer">Jira issues for this resource</a></li>
                  <li><a href="https://eidc.ceh.ac.uk/metadata/${id?html}/" target="_blank" rel="noopener noreferrer">DRH admin folder</a></li>
                </#if>
              </ul>
          </div>
          </#if>
        </#if>
      </div> 
    </#if>
  </div>
</#if>