<#if permission.userCanEdit(id)>
<div class="container">
    <div class="sensor-admin col-md-6">
        <a href="/elter/documents" class="btn btn-success">
            <i class="fa fa-files-o" aria-hidden="true"></i>
            <span>eLTER Documents</span>
        </a>
    </div>
    <div class="sensor-admin col-md-6">
        <div class="btn-group btn-group-justified" role="group">
            <a href='#' class="toggle-edit btn btn-success">
                <i class="fa fa-pencil" aria-hidden="true"></i>
                <span>Edit</span>
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