<#if permission.userCanEdit(id)>
<div class="container">
    <div class="col-md-8">
        <a href="/elter/documents" class="btn btn-lg btn-link">
            <i class="fa fa-files-o" aria-hidden="true"></i>
            <span>Documents</span>
        </a>
    </div>
    <div class="sensor-admin col-md-4">
        <div class="btn-group btn-group-justified" role="group">
            <a href="#" data-document-type="${metadata.documentType}" class="btn btn-success edit-control">
                <i class="fa fa-pencil" aria-hidden="true"></i>
                <span>Edit</span>
            </a>
            <a href="/documents/${id?html}/permission" data-document-type="${metadata.documentType}" class="btn btn-success">
                <i class="fa fa-unlock-alt" aria-hidden="true"></i>
                <span>Amend Permissions</span>
            </a>
        </div>
    </div>
</div>
</#if>