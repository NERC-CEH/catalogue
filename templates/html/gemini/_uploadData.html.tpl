<#if metadata.catalogue == "eidc">
<#if permission.userCanEdit(id)>
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
</#if>