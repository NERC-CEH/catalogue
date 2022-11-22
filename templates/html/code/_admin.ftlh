<#if permission.userCanEdit(id)>
 <div class="hidden-print adminToolbar" role="toolbar">
    <div class="btn-group">
      <a href="#" class="btn btn-default edit-control" data-document-type="${metadata.documentType}">Edit</a>
      <div class="btn-group" role="group">
          <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">Publish <span class="caret"></span></button>
          <ul class="dropdown-menu dropdown-menu-right">
            <li><a href="/documents/${id}/permission">Amend permissions</a></li>
            <li><a href="/documents/${id}/publication">Publication status</a></li>
            <li><a href="/documents/${id}/catalogue" class="catalogue-control">Amend catalogues</a></li>
          </ul>
      </div>
    </div>
</div>
</#if>
