<#if permission.userCanEdit(id)>
<div class="row d-print-none" id="adminPanel">
    <div class="text-end" id="adminToolbar" role="toolbar">
      <div class="btn-group btn-group-sm">
        <button type="button" class="btn btn-light border btn-wide edit-control"  data-document-type="${metadata.documentType}">Edit</button>
        <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
        <button type="button" class="btn btn-light border dropdown-toggle" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
          <span class="caret"></span>
          <span class="visually-hidden">Toggle Dropdown</span>
        </button>
        <ul class="dropdown-menu dropdown-menu-end">
          <li><a class="dropdown-item" href="/documents/${id}/permission"><i class="fa-solid fa-users"></i> Permissions</a></li>
          <li><a class="dropdown-item" href="/documents/${id}/publication"><i class="fa-solid fa-eye"></i> Publication status</a></li>
          <li role="separator" class="divider"></li>
          <li><a href="/documents/${id}/catalogue" class="dropdown-item catalogue-control"><i class="fa-solid fa-right-from-bracket"></i> Move to a different catalogue</a></li>
        </ul>
        </#if>
      </div>
    </div>
</div>
</#if>
