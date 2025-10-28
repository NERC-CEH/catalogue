<#if metadata.catalogue == "eidc" && permission.userCanUpload(id) && !permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN") && metadata.documentType != 'LINK_DOCUMENT' && (resourceType.value == 'dataset' | resourceType.value == 'nonGeographicDataset' | resourceType.value == 'software' | resourceType.value == 'model' )>
  <div class="card card-default d-print-none" id="document-upload-panel">
    <a href="/upload/${id}">
      <div class="card-body text-center">
        <p>
            <i class="fa-solid fa-arrow-circle-up fa-2x"></i>
            <span>UPLOAD DATA</span>
        </p>
      </div>
    </a>
  </div>
</#if>
