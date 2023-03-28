<#import "../../functions.ftlh" as func>
<#assign
  isDraft = metadata.state == 'draft'
  isEidcCatalogue = metadata.catalogue == "eidc"
  isEidcCustodian = func.filter(custodians![], "organisationName", "NERC EDS Environmental Information Data Centre")?size gt 0
  isEidcWithResourceType=isEidcCatalogue && (resourceType.value == 'dataset' || resourceType.value == 'nonGeographicDataset' || resourceType.value == 'application')
  isNotLinkDocument = metadata.documentType != 'LINK_DOCUMENT'
  isRestrictedFieldsEditor = permission.userCanEditRestrictedFields(metadata.catalogue)
  isSimpleUpload = !isEidcCatalogue && permission.userCanUpload(id)
  isServiceAgreement = profile.isActive("service-agreement") && isNewServiceAgreement()

>

<#assign hasCitation = 0>
<#if citation?? && citation?has_content>
  <#if citation.doi?? && citation.doi?matches("^10.5285/[\\w-]+$")>
    <#assign hasCitation = 1>
  </#if>
</#if>

<#if onlineResources?has_content>
  <#assign
    orderResources = func.filter(onlineResources, "function", "order")
    legacyOrders = func.filterRegex(orderResources, "url", "http://catalogue.ceh.ac.uk/download")
    newOrders = func.filterRegex(orderResources, "url", "https://order-eidc.ceh.ac.uk/resources/")
    orders = legacyOrders + newOrders
    hasOrders = orders?size gt 0
  >
<#else>
  <#assign
    orders = []
    hasOrders = false
  >
</#if>

<#if resourceType?? && permission.userCanEdit(id)>
  <div class="row hidden-print" id="adminPanel">
    <div class="text-right" id="adminToolbar" role="toolbar">
      <div class="btn-group btn-group-sm">
        <button type="button" class="btn btn-default btn-wide edit-control" data-document-type="${metadata.documentType}">
          Edit
        </button>
        <#if isRestrictedFieldsEditor>
          <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" >
            <span class="caret"></span>
            <span class="sr-only">Toggle Dropdown</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-right">
            <li>
              <a href="/documents/${id}/permission">
                <i class="fa-solid fa-users"></i>
                Permissions
              </a>
            </li>
            <li>
              <a href="/documents/${id}/publication">
                <i class="fa-solid fa-eye"></i>
                Publication status
              </a>
            </li>
            <#if isEidcWithResourceType && isServiceAgreement>
              <li class="service-agreement" data-id="${id}" id="service-agreement-gemini">
                <a href="/service-agreement/${id}">
                  <i class="fa-solid fa-file"></i>
                  Service agreement
                </a>
              </li>
            </#if>
            <li role="separator" class="divider"></li>
            <li>
              <a href="/documents/${id}/catalogue"
                class="catalogue-control"
              >
                <i class="fa-solid fa-right-from-bracket"></i>
                Move to a different catalogue
              </a>
            </li>
          </ul>
        </#if>
      </div>

      <#if isRestrictedFieldsEditor>
        <#if isNotLinkDocument>

          <#-- DOI button -->
          <#if hasCitation = 0>
            <#if isEidcWithResourceType>
              <div class="btn-group btn-group-sm" role="group">
                <button type="button" class="btn btn-default btn-wide dropdown-toggle" data-toggle="dropdown">
                  <#if !datacitable>
                    <i class='fa-solid fa-triangle-exclamation text-red'></i>
                  </#if>
                  DOI <span class="caret"></span>
                </button>
                <ul class="dropdown-menu dropdown-menu-right">
                  <#if dataciteMintable>
                    <li>
                      <div class="msg text-success">
                        <i class="fa-solid fa-check text-success"></i>
                        There is enough information<br>to assign a DOI to this resource
                      </div>
                    </li>
                    <#if permission.userCanDatacite()>
                      <form action="/documents/${id}/datacite" method="POST" class="mintDOI">
                          <button type="submit" class="btn btn-success" value="Mint DOI">Mint DOI</button>
                      </form>
                    </#if>
                    <li class="divider"></li>
                  <#elseif !datacitable>
                    <li>
                      <div class="msg text-red">
                        <i class="fa-solid fa-exclamation"></i>
                        Conditions for assigning a <br>DOI have not been met
                      </div>
                    </li>
                    <li>
                      <a href="https://eidc.ceh.ac.uk/admin/guidance/mintADOId" target="_blank" rel="noopener noreferrer" >
                        Help
                      </a>
                    </li>
                  </#if>
                </ul>
              </div>
            </#if>
          </#if>

          <#-- 'More' button -->
          <#if isSimpleUpload || hasOrders || isEidcCustodian || isEidcWithResourceType>
            <div class="btn-group btn-group-sm" role="group">
              <button type="button"  class="btn btn-default btn-wide dropdown-toggle" data-toggle="dropdown">
                More <span class="caret"></span>
              </button>
              <ul class="dropdown-menu dropdown-menu-right">
                <#if isSimpleUpload || isEidcWithResourceType>
                  <li>
                    <a href="/upload/${id}">Manage data</a>
                  </li>
                  <li>
                    <a href="${uri}?format=ceda">CEDA Archive yaml file</a>
                  </li>
                </#if>
                <#list orders>
                  <#items as testLink>
                    <#if testLink.function == "order">
                      <li>
                        <a href="${testLink.url}?test=true">Test order</a>
                      </li>
                    </#if>
                  </#items>
                </#list>
                <#if isEidcCatalogue && (isDraft || isEidcCustodian)>
                  <li>
                    <a href="https://jira.ceh.ac.uk/issues/?jql=cf%5B13250%5D%3D%22${id}%22"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                      Jira issues for this resource
                    </a>
                  </li>
                  <li>
                    <a href="https://eidc.ceh.ac.uk/metadata/${id}/"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                      DRH admin folder
                    </a>
                  </li>
                </#if>
              </ul>
            </div>
          </#if>

        </#if>
      </#if>

    </div>
  </div>
</#if>

<#function isNewServiceAgreement>
  <#if metadata.state == "draft">
    <#return true>
  </#if>
  <#if datasetReferenceDate??>
    <#if datasetReferenceDate.creationDate??>
      <#return geminiHelper.isNewServiceAgreement(datasetReferenceDate.creationDate)>
    </#if>
  </#if>
  <#return false>
</#function>
