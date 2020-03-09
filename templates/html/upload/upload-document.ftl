<#import "../skeleton.ftl" as skeleton>
<#assign issues=jira.search("project%3Deidchelp%20and%20component%3D%27data%20transfer%27%20and%20cf%5B13250%5D%3D" + id)>
<#assign open=issues?size == 1 && issues[0].status == 'open'>
<#assign scheduled=issues?size == 1 && issues[0].status == 'scheduled'>
<#assign inProgress=issues?size == 1 && issues[0].status == 'in progress'>

<@skeleton.master title=title>
<div class="container" id="document-upload" data-guid='${id}'>

    <#if scheduled>
    <div class="container-fluid document-upload is-scheduled">
    <#else>
    <div class="container-fluid document-upload">
    </#if>
        <div class="return">
            <a href="/documents/${id}">
                <i class="btn-icon fas fa-arrow-left"></i>
                <span>RETURN TO DOCUMENT: ${id}<span>
            </a>
        </div>

        <#if issues?size != 1 >
            <div class="no-issue alert alert-danger"><b>ERROR</b><br>There is no Jira issue for this deposit</div>
        <#elseif scheduled>
            <section class="document-upload-section">
                <div class="page-header">
                    <h3><i class="btn-icon fas fa-file-upload"></i> Upload</h3>
                </div>
                <div class='dropzone-container'>
                    <div class="dropzone-files">
                        <div class="row file-head">
                            <div class="col-md-7">
                                <div class="row">
                                    <div class="col-md-6"><b>Name</b></div>
                                    <div class="col-md-2"><b>Size</b></div>
                                    <div class="col-md-4"><b>Status</b></div>
                                </div>
                            </div>
                            <div class="file-head-action col-md-5"><b>Actions</b></div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-7"></div>
                        <div class="col-md-2">
                            <button class="btn btn-success fileinput-button">
                                <i class="btn-icon fas fa-file-upload"></i>
                                <span>ADD FILES ...</span>
                            </button>
                        </div>
                        <div class="col-md-3">
                        <button class="btn btn-primary finish" >
                            <i class="btn-icon fas fa-check"></i>
                            <span>FINISH UPLOADING</span>
                        </button>
                    </div>
                    </div>
                </div>
            </section>
        </#if>

        <section class="document-upload-section">
            <div class="page-header">
                <h3><i class="btn-icon fas fa-archive"></i> Data</h3>
            </div>
            <#assign pagId="documents">
            <#include "_pagination.ftl">
            <div class="row loading text-center">
                <h3><i class="fas fa-spinner fa-spin-2x"></i> LOADING</h3>
            </div>
            <div class="documents-files"></div>
            <#if inProgress && permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
            <div class="row">
                <div class="col-md-6"></div>
                <div class="col-md-3">
                    <button class="data-action move-all btn btn-success"><i class="btn-icon fas fa-level-down-alt"></i><span>MOVE ALL DATASTORE</span></button>
                </div>
                <div class="col-md-3">
                    <button class="data-action reschedule btn btn-success"><i class="btn-icon fas fa-redo"></i><span>RESCHEDULE</span></button>
                </div>
            </div>
            <#elseif open && permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
            <div class="row">
                <div class="col-md-6"></div>
                <div class="col-md-3">
                    <button class="data-action move-all btn btn-success"><i class="btn-icon fas fa-level-down-alt"></i><span>MOVE ALL DATASTORE</span></button>
                </div>
                <div class="col-md-3">
                    <button class="data-action schedule btn btn-success"><i class="btn-icon far fa-calendar-check"></i><span>SCHEDULE</span></button>
                </div>
            </div>
            <#elseif permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
            <div class="row">
                <div class="col-md-9"></div>
                <div class="col-md-3">
                    <button class="data-action move-all btn btn-success"><i class="btn-icon fas fa-level-down-alt"></i><span>MOVE ALL DATASTORE</span></button>
                </div>
            </div>
            </#if>
        </section>

<#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
        <section class="document-upload-section">
            <div class="page-header">
                <h3><i class="btn-icon far fa-copy"></i> Metadata <small>Supporting Documents</small></h3>
            </div>
            <#assign pagId="supporting-documents">
            <#include "_pagination.ftl">
            <div class="row loading text-center">
                <h3><i class="fas fa-spinner fa-spin-2x"></i> LOADING</h3>
            </div>
            <div class="supporting-documents-files"></div>
        </section>

        <section class="document-upload-section">
            <div class="page-header">
                <h3><i class="btn-icon fas fa-lock"></i> Datastore</h3>
            </div>
            <#assign pagId="datastore">
            <#include "_pagination.ftl">
            <div class="row loading text-center">
                <h3><i class="fas fa-spinner fa-spin-2x"></i> LOADING</h3>
            </div>
            <div class="datastore-files"></div>
        </section>
</#if>
        <section class="document-upload-section">
            <div class="row">
                <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
                <div class="col-md-6"></div>
                <div class="col-md-6 file-actions">
                    <button class="validate-all file-action btn btn-primary"><i class="btn-icon fas fa-check"></i><span>VALIDATE ALL</span></button>
                    <a class="downloadChecksum file-action btn btn-success" href="documents/csv/${id}" download="checksums_${id}.csv"><i class="btn-icon fas fa-file-download"></i><span>DOWNLOAD CHECKSUM REPORT</span></a>
                </div>
                <#else>
                <div class="col-md-9"></div>
                <div class="col-md-3">
                    <a class="downloadChecksum btn btn-success" href="documents/csv/${id}" download="checksums_${id}.csv"><i class="btn-icon fas fa-file-download"></i><span>DOWNLOAD CHECKSUM REPORT</span></a>
                </div>
                </#if>
            </div>
        </section>
    </div>
</div>
<div class="modal fade" id="documentUploadModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"></h4>
            </div>
            <div class="modal-body"></div>
            <div class="modal-footer">
                <button class="btn btn-default modal-dismiss" data-dismiss="modal">NO</button>
                <button class="btn btn-danger modal-accept"  data-dismiss="modal">YES</button>
            </div>
        </div>
    </div>
</div>
</@skeleton.master>
