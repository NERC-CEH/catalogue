<#if permission.userCanEdit(id)>
  <div class="row hidden-print" id="adminPanel">
    <div class="text-right" id="adminToolbar" role="toolbar">
      <div class="btn-group btn-group-sm" role="group">
        <a href="#" class="btn btn-default btn-wide edit-control" data-document-type="${metadata.documentType}">Edit</a>
        <div class="btn-group btn-group-sm" role="group">
          <button type="button" class="btn btn-default btn-wide dropdown-toggle" data-toggle="dropdown">Publish <span class="caret"></span></button>
            <ul class="dropdown-menu dropdown-menu-right">
              <li><a href="/documents/${id}/permission">Amend permissions</a></li>
              <li><a href="/documents/${id}/catalogue" class="catalogue-control">Amend catalogues</a></li>
              <li><a href="/documents/${id}/publication">Publication status</a></li>
            </ul>
        </div>
      </div>
    </div>
  </div>
</#if>