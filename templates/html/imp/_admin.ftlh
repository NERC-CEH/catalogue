<#if permission.userCanEdit(id)>
  <div id="adminPanel" class="panel d-print-none">
    <div class="panel-heading"><p class="panel-title">Admin</p></div>
    <div class="panel-body">
      <div class="btn-group btn-group-sm btn-group-justified" role="group">
        <a href="#" class="btn btn-light border edit-control" data-document-type="${metadata.documentType}">Edit</a>
        <div class="btn-group btn-group-sm" role="group">
          <button type="button" class="btn btn-light border dropdown-toggle" data-bs-toggle="dropdown">Publish</button>
            <ul class="dropdown-menu dropdown-menu-end">
              <li><a class="dropdown-item" href="/documents/${id}/permission">Amend permissions</a></li>
              <li><a href="/documents/${id}/catalogue" class="dropdown-item catalogue-control">Amend catalogues</a></li>
              <li><a class="dropdown-item" href="/documents/${id}/publication">Publication status</a></li>
            </ul>
        </div>
      </div>
    </div>
  </div>
</#if>
