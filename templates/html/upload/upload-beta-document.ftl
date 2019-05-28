<#import "../skeleton.ftl" as skeleton>
<#assign issues=jira.search("project%3Deidchelp%20and%20component%3D%27data%20transfer%27%20and%20labels%3D" + id)>
<#assign scheduled=issues?size == 1 && issues[0].status == 'scheduled'>

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
                <div class='dropzone'>
                    <div class="dropzone-files">
                        <div class="row file-head">
                            <div class="col-md-9">
                                <div class="row">
                                    <div class="col-md-6"><b>Name</b></div>
                                    <div class="col-md-3"><b>Size</b></div>
                                    <div class="col-md-3"><b>Status</b></div>
                                </div>
                            </div>
                            <div class="file-head-action col-md-3"><b>Actions</b></div>
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
                        <button class="btn btn-primary finish">
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
            <div class="row loading text-center">
                <h3><i class="fas fa-spinner fa-spin-2x"></i> LOADING</h3>
            </div>
            <div class="documents-files"></div>
            <div class="row">
                <div class="col-md-9"></div>
                <div class="col-md-3">
                    <button class="move-all btn btn-success"><i class="btn-icon fas fa-level-down-alt"></i><span>MOVE ALL DATASTORE</span></button>
                </div>
            </div>
        </section>

        <section class="document-upload-section">
            <div class="page-header">
                <h3><i class="btn-icon far fa-copy"></i> Metadata <small>Supporting Documents</small></h3>
            </div>
            <div class="row loading text-center">
                <h3><i class="fas fa-spinner fa-spin-2x"></i> LOADING</h3>
            </div>
            <div class="plone-files"></div>
        </section>

        <section class="document-upload-section">
            <div class="page-header">
                <h3><i class="btn-icon fas fa-lock"></i> Datastore</h3>
            </div>
            <div class="row loading text-center">
                <h3><i class="fas fa-spinner fa-spin-2x"></i> LOADING</h3>
            </div>
            <div class="datastore-files"></div>
            <div class="row">
                <div class="col-md-10"></div>
                <div class="col-md-2">
                    <button class="zip btn btn-success"><i class="btn-icon far fa-file-archive"></i><span>ZIP</span></button>
                    <button class="unzip btn btn-success" style="display: none"><i class="btn-icon far fa-file-archive"></i><span>UNZIP</span></button>
                </div>
            </div>
        </section>

        <section class="document-upload-section">
            <div class="row">
                <div class="col-md-6"></div>
                <div class="col-md-6 file-actions">
                    <button class="validate-all file-action btn btn-primary"><i class="btn-icon fas fa-check"></i><span>VALIDATE ALL</span></button>
                    <a class="downloadChecksum file-action btn btn-success" href="get_checksum" download="checksums_${id}.csv"><i class="btn-icon fas fa-file-download"></i><span>DOWNLOAD CHECKSUM REPORT</span></a>
                </div>
            </div>
        </section>
    </div>
</div>
</@skeleton.master>