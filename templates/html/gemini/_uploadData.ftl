<#if metadata.catalogue == "eidc" && (permission.userCanUpload(id) || permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")) && metadata.documentType != 'LINK_DOCUMENT' && (resourceType.value == 'dataset' | resourceType.value == 'nonGeographicDataset' | resourceType.value == 'application')>
  <div class="panel panel-default hidden-print" id="document-upload-panel">
    <div class="panel-body text-center">

      <#if permission.userCanUpload(id)>
        <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
          <#assign uploadText="Manage data" uploadIcon="wrench">
        <#else>
          <#assign uploadText="Upload data" uploadIcon="upload">
        </#if>
      <#else>
        <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
          <#assign uploadText="View data" uploadIcon="eye">
        </#if>
      </#if>

      <p>
        <a href="/upload/${id}">
          <i class="fas fa-${uploadIcon}"></i>
          <span>${uploadText}</span>
        </a>
      </p>

    </div>
  </div>

</#if>