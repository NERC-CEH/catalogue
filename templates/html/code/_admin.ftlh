<#if permission.userCanEdit(id)>
 <div class="d-print-none adminToolbar" role="toolbar">
    <div class="btn-group">
      <a href="#" class="btn btn-light border btn-wide btn-sm edit-control" data-document-type="${metadata.documentType}">Edit</a>
      <div class="btn-group" role="group">
          <button type="button" class="btn btn-light border btn-wide btn-sm dropdown-toggle" data-bs-toggle="dropdown">Publish <span class="caret"></span></button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li><a class="dropdown-item" href="/documents/${id}/permission">Amend permissions</a></li>
            <li><a class="dropdown-item" href="/documents/${id}/publication">Publication status</a></li>
            <li><a href="/documents/${id}/catalogue" class="dropdown-item catalogue-control">Amend catalogues</a></li>
          </ul>
      </div>
    </div>
</div>
</#if>
