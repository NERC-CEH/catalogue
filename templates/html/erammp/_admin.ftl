<#if permission.userCanEdit(id)>
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
    </div>
</div>
</#if>