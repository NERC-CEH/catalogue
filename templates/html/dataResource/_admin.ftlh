<#assign
  isDraft = metadata.state == 'draft'
  isEidcCatalogue = metadata.catalogue == "eidc"
  isEidcCustodian = (isEidcCustodian?? && isEidcCustodian())!false
  isEidcWithResourceType=isEidcCatalogue && (resourceType.value == 'dataset' || resourceType.value == 'nonGeographicDataset' || resourceType.value == 'model' || resourceType.value == 'method' || resourceType.value == 'software' || resourceType.value == 'computationalNotebook')
  isNotLinkDocument = metadata.documentType != 'LINK_DOCUMENT'
  isRestrictedFieldsEditor = permission.userCanEditRestrictedFields(metadata.catalogue)
  isSimpleUpload = !isEidcCatalogue && permission.userCanUpload(id)
>

<#assign hasCitation = 0>
<#if citation?? && citation?has_content>
  <#if citation.doi?? && citation.doi?matches("^10.5285/[\\w-]+$")>
    <#assign hasCitation = 1>
  </#if>
</#if>

<#if resourceType?? && permission.userCanEdit(id)>
  <div class="row d-print-none" id="adminPanel">
    <div class="d-flex gap-2 justify-content-end flex-wrap adminToolbar" role="toolbar">

      <@editButton />

      <#if isRestrictedFieldsEditor && isNotLinkDocument>
        <#if hasCitation = 0  && isEidcWithResourceType && metadata.catalogue == "eidc">
          <@doiButton />
        </#if>

        <@cloneRecord />

        <#if isSimpleUpload|| isEidcCustodian || isEidcWithResourceType>
          <@moreButton />
        </#if>
      </#if>

    </div>
  </div>
</#if>

<#-- macros -->
<#macro menuLink link  icon="" target="_self">
  <li><a class="dropdown-item" href="${link}" target="${target}" rel="noopener noreferrer">
    <#if icon?has_content>
      <i class="fa-fw ${icon}"></i>
    </#if>
     <#nested>
  </a></li>
</#macro>

<#macro cloneRecord>
  <#if permission.userCanEdit(id) && state == 'published'>
    <form action="/documents/${id}/clone"
          method="POST"
          class="d-none clone-control"><#-- temporarily disabled by adding class d-none -->
      <button type="submit" class="btn btn-sm btn-outline-secondary btn-wide">
        New version
      </button>
    </form>
  </#if>
</#macro>

<#macro editButton>
  <div class="btn-group btn-group-sm position-relative">
    <button type="button" class="btn btn-outline-secondary btn-wide edit-control" data-document-type="${metadata.documentType}">
      Edit
      <#if totalIssues gte 1 && metadata.catalogue == "eidc">
        <span class="position-absolute top-0 start-0 translate-middle badge rounded-pill bg-${metadataClass}"
        data-bs-toggle="tooltip" data-bs-placement="right" title="Metadata issues">
          ${totalIssues}
          <span class="visually-hidden">Metadata issues</span>
        </span>
      </#if>
    </button>
    <#if isRestrictedFieldsEditor>
      <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false" >
        <span class="caret"></span>
        <span class="visually-hidden">Toggle Dropdown</span>
      </button>
      <ul class="dropdown-menu dropdown-menu-end">

        <#if totalIssues gte 1>
          <@menuLink "#segment-metadataQuality">
            <span class="badge rounded-pill bg-${metadataClass}">${totalIssues}</span>
            <span class="text-${metadataClass}">metadata issue<#if totalIssues gt 1>s</#if></span>
          </@menuLink>
          <li class="dropdown-divider"></li>
         </#if>

        <@menuLink "/documents/${id}/permission" "fa-solid fa-users">Permissions</@menuLink>

        <#if metadata.state!="published" && errors gte 1 && catalogue.id=="eidc" >
          <li class="disabled dropdown-item" title="clear errors before publishing"><i class="fa-solid fa-ban fa-fw"></i> Publication status</li>
        <#else>
          <@menuLink "/documents/${id}/publication" "fa-solid fa-eye">Publication status</@menuLink>
        </#if>

        <span class="catalogue-control">
          <@menuLink "/documents/${id}/catalogue" "fa-solid fa-right-from-bracket">Move to a different catalogue</@menuLink>
        </span>
      </ul>
    </#if>
  </div>
</#macro>

<#macro moreButton>
  <div class="btn-group btn-group-sm" role="group">
    <button type="button" class="btn btn-outline-secondary btn-wide dropdown-toggle" data-bs-toggle="dropdown">
      More <span class="caret"></span>
    </button>
    <ul class="dropdown-menu dropdown-menu-end">
      <#if isSimpleUpload || isEidcWithResourceType>
        <@menuLink "/upload/${id}">Manage data</@menuLink>
      </#if>

      <#if isEidcCatalogue>
        <#if hasOnlineServiceAgreement!false>
         <li class="dropdown-divider"></li>
         <li class="service-agreement" data-id="${id}" id="service-agreement-gemini"><a class="dropdown-item" href="/service-agreement/${id}">Service agreement</a></li>
        </#if>
      </#if>

      <#if isEidcCatalogue && (isDraft || isEidcCustodian)>
        <li class="dropdown-divider"></li>
        <@menuLink link="https://jira.ceh.ac.uk/issues/?jql=cf%5B13250%5D%3D%22${id}%22" target="_blank">Jira issues for this resource</@menuLink>
        <@menuLink link="https://eidc.ceh.ac.uk/metadata/${id}" target="_blank">DRH admin folder</@menuLink>
      </#if>

      <#list orders as order>
        <@menuLink link="${order.url}?test=true">Test order</@menuLink>
      </#list>
    </ul>
  </div>
</#macro>

<#macro doiButton>
  <div class="btn-group btn-group-sm" role="group">
    <button type="button" class="position-relative btn btn-outline-secondary btn-wide dropdown-toggle" data-bs-toggle="dropdown">
      <#if !datacitable>
        <i class="fa-solid fa-triangle-exclamation fa-xl text-danger position-absolute top-0 start-0"></i>
      </#if>
      DOI <span class="caret"></span>
    </button>
    <ul class="dropdown-menu dropdown-menu-end">
      <#if dataciteMintable>
        <li>
          <div class="msg text-success">
            <i class="fa-solid fa-check text-success"></i> There is enough information<br>to assign a DOI to this resource
          </div>
        </li>
        <#if permission.userCanDatacite()>
          <form action="/documents/${id}/datacite" method="POST" class="mintDOI">
              <button type="submit" class="btn btn-success" value="Mint DOI">Mint DOI</button>
          </form>
        </#if>
      <#elseif !datacitable>
        <li>
          <div class="msg text-danger">
            <i class="fa-solid fa-exclamation"></i> Conditions for assigning a <br>DOI have not been met
          </div>
        </li>
        <li>
          <a class="dropdown-item" href="https://eidc.ceh.ac.uk/admin/guidance/mintADOId" target="_blank" rel="noopener noreferrer">
            Help
          </a>
        </li>
      </#if>
    </ul>
  </div>
</#macro>
