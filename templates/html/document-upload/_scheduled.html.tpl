<section class="section">
    <p class="alert alert-success" role="alert">
        <b>Scheduled</b>
        <br />Add your files to <u>Documents</u> in order to upload them
        <br />Select <b>Finish</b> when you have finished uploading
    </p>
    <div class="messages alert alert-info" role="alert">
        <div class="message loading">
            <span class="fa fa-refresh fa-spin"></span>
            <span>Loading please wait ...</span>
        </div>
    </div>
</section>
<section class="section">
    <div class="container-fluid folders scheduled">
        <div class="row">
            <div class="documents folder">
                <div class="folder-title">
                    <span class="folder-name">
                        <i class="fa fa-folder"></i> Documents
                    </span>
                </div>
                <div class="dropzone-files files connectedSortable">
                    <#if documents.getFiles()?size == 0>
                        <div class="empty-message">Drag files into <u>here</u> to upload</div>
                    <#else>
                        <div class="empty-message"></div>
                    </#if>
                    <#list documents.getFiles() as file>
                        <div id="documents-${file.id}" class="file btn btn-primary">
                            <p class="filename">
                                <i class="fa fa-file-text-o"></i> <span class="filename-label">${file.name}</span>
                            </p>
                            <div class="file-options text-right">
                                <button class="btn btn-danger delete" disabled data-toggle="modal" data-target="#documentUploadModal">Delete</button>
                            </div>
                        </div>
                    </#list>
                </div>
                <div class="folder-options text-right">
                    <button class="btn btn-success fileinput-button" disabled>Add files ...</button>
                    <button class="btn btn-warning finish" disabled data-toggle="modal" data-target="#documentUploadModal">Finish</button>
                </div>
            </div>
        </div>
    </div>
</section>
<#include "_modal.html.tpl" />