<#if metadata.catalogue == "eidc" && permission.userCanEdit(id) && metadata.documentType != 'LINK_DOCUMENT' && (resourceType.value == 'dataset' | resourceType.value == 'nonGeographicDataset' | resourceType.value == 'application')>
  <div class="panel panel-default hidden-print" id="document-upload-panel">
    <div class="panel-body text-center">
      <#if permission.userCanUpload(id)>
        <h4><b>
        <a href="/upload/${id}">
          <i class="fa fa-upload text-info"></i>
          <span>Upload data</span>
        </a>
        </b></h4>
      <#else>
        <a href="/upload/${id}">
          <i class="fa fa-files-o text-info"></i>
           <span>View/manage data</span>
        </a>
      </#if>
      </div>
  </div>
</#if>