<#if permission.userCanEdit(id)>
  <div class="panel panel-default hidden-print" id="document-upload-panel">
    <div class="panel-heading">
    <#if permission.userCanUpload(id)>
      <p class="panel-title">Upload documents</p>
    <#else>
      <p class="panel-title">View documents</p>
    </#if>
    </div>
    <div class="panel-body">
      <a href="/upload/${id}">
        <i class="fa fa-upload text-info"></i>
        <span>Manage Files</span>
      </a>
    </div>
  </div>
</#if>