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
          <li><a href="/documents/${id}/permission"><i class="fa-solid fa-users"></i> Permissions</a></li>
          <li><a href="/documents/${id}/publication"><i class="fa-solid fa-eye"></i> Publication status</a></li>
          <li role="separator" class="divider"></li>
          <li><a href="/documents/${id}/catalogue" class="catalogue-control"><i class="fa-solid fa-right-from-bracket"></i> Move to a different catalogue</a></li>
        </ul>
        </#if>
      </div>
    </div>
</div>
</#if>
