<#if permission.userCanUpload(id)>
  <div class="panel panel-default hidden-print" id="document-distribution">
    <div class="panel-heading">
      <p class="panel-title">Upload documents</p>
    </div>
    <div class="panel-body">
      <a href="/documents/${id}/upload">
        <i class="glyphicon glyphicon-open text-info"></i>
        <span>Upload</span>
      </a>
    </div>
  </div>
</#if>
