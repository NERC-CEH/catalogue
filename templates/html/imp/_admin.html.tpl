<#if permission.userCanEdit(id)>
  <div id="adminPanel" class="panel hidden-print">
    <div class="panel-heading"><p class="panel-title">Admin</p></div>
    <div class="panel-body">
      <div class="btn-group btn-group-sm btn-group-justified" role="group">
        <a href="#" class="btn btn-default edit-control" data-document-type="${metadata.documentType}">Edit</a>
        <div class="btn-group btn-group-sm" role="group">
          <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">Publish <span class="caret"></span></button>
            <ul class="dropdown-menu dropdown-menu-right">
              <li><a href="/documents/${id}/permission">Amend permissions</a></li>
              <li><a href="/documents/${id}/catalogue">Amend catalogues</a></li>
              <li><a href="/documents/${id}/publication">Publication status</a></li>
            </ul>
        </div>
      </div>
    </div>
  </div>
</#if>