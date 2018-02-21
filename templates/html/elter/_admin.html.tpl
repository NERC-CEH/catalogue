<#if permission.userCanEdit(id)>
<div id="deleteModal" class="modal fade" tabindex="-1" role="dialog">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Delete: ${title}</h4>
      </div>
      <div class="modal-body">
        <p>Are you sure you wish to delete <b>${id}</b>?</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">No</button>
        <button type="button" class="delete-document btn btn-danger">Yes</button>
      </div>
    </div>
  </div>
</div>
<div class="container new-admin">
    <div class="admin col-md-6">
        <a href="/elter/documents" class="btn btn-link">
            <i class="fa fa-files-o" aria-hidden="true"></i>
            <span>eLTER Documents</span>
        </a>
    </div>
    <div class="admin col-md-6">
        <div class="btn-group btn-group-justified" role="group">
            <a href='#' class="btn btn-danger" data-toggle="modal" data-target="#deleteModal">
                <i class="fa fa-trash" aria-hidden="true"></i>
                <span>Delete</span>
            </a>
            <a href="/documents/${id?html}/permission" data-document-type="${metadata.documentType}" class="btn btn-success">
                <i class="fa fa-unlock-alt" aria-hidden="true"></i>
                <span>Amend Permissions</span>
            </a>
            <a href="/documents/${id?html}/publication" data-document-type="${metadata.documentType}" class="btn btn-success">
                <i class="fa fa-check" aria-hidden="true"></i>
                <span>Publication</span>
            </a>
        </div>
    </div>
</div>
</#if>