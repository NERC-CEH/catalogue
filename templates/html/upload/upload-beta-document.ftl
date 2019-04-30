<#import "../skeleton.ftl" as skeleton>
<#assign issues=jira.search("project=eidchelp and component='data transfer' and labels=" + id)>

<@skeleton.master title=title>
<div class="container" id="document-upload" data-guid='${id}'>
    <div class="container-fluid document-upload">
        <div>
            <a class="btn btn-success" href="/documents/${id}"><i class="btn-icon fas fa-arrow-left"></i><span>${id}<span></a>
        </div>
<#--  
        <section class="document-upload-section">
            <div class="page-header">
                <h3>Upload</h3>
            </div>
        </section>  -->

        <section class="document-upload-section">
            <div class="page-header">
                <h3><i class="btn-icon fas fa-archive"></i> Data</h3>
            </div>
            <div class="row file-head">
                <div class="col-md-8">
                    <div class="row">
                        <div class="col-md-4"><b>Name</b></div>
                        <div class="col-md-2"><b>Size</b></div>
                        <div class="col-md-6"><b>Checksum</b></div>
                    </div>
                </div>
                <div class="file-head-action col-md-3"><b>Actions</b></div>
            </div>
            <div class="row loading text-center">
                <h3><i class="fas fa-spinner fa-spin"></i> LOADING</h3>
            </div>
            <div class="documents-files"></div>
            <div class="row">
                <div class="col-md-8"></div>
                <div class="col-md-4">
                    <button class="move-all btn btn-success"><i class="btn-icon fas fa-level-down-alt"></i><span>MOVE ALL DATASTORE</span></button>
                </div>
            </div>
        </section>

        <section class="document-upload-section">
            <div class="page-header">
                <h3><i class="btn-icon far fa-copy"></i> Metadata <small>Supporting Documents</small></h3>
            </div>
            <div class="row file-head">
                <div class="col-md-8">
                    <div class="row">
                        <div class="col-md-4"><b>Name</b></div>
                        <div class="col-md-2"><b>Size</b></div>
                        <div class="col-md-6"><b>Checksum</b></div>
                    </div>
                </div>
                <div class="file-head-action col-md-3"><b>Actions</b></div>
            </div>
            <div class="row loading text-center">
                <h3><i class="fas fa-spinner fa-spin"></i> LOADING</h3>
            </div>
            <div class="plone-files"></div>
        </section>

        <section class="document-upload-section">
            <div class="page-header">
                <h3><i class="btn-icon fas fa-lock"></i> Datastore</h3>
            </div>
            <div class="row file-head">
                <div class="col-md-8">
                    <div class="row">
                        <div class="col-md-4"><b>Name</b></div>
                        <div class="col-md-2"><b>Size</b></div>
                        <div class="col-md-6"><b>Checksum</b></div>
                    </div>
                </div>
                <div class="file-head-action col-md-3"><b>Actions</b></div>
            </div>
            <div class="row loading text-center">
                <h3><i class="fas fa-spinner fa-spin"></i> LOADING</h3>
            </div>
            <div class="datastore-files"></div>
        </section>

        <section class="document-upload-section">
            <div class="row">
                <div class="col-md-6"></div>
                <div class="col-md-6 file-actions">
                    <button class="file-action btn btn-primary"><i class="btn-icon fas fa-check"></i><span>VALIDATE ALL</span></button>
                    <button class="file-action btn btn-success"><i class="btn-icon far fa-file-archive"></i><span>ZIP</span></button>
                    <button class="file-action btn btn-success"><i class="btn-icon fas fa-file-download"></i><span>DOWNLOAD CHECKSUM REPORT</span></button>
                </div>
            </div>
        </section>
    </div>
</div>
</@skeleton.master>