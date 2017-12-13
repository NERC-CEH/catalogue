<section class="section">
    <div class="intro">
        <h2>Upload your data</h2>
        <p>Drag and drop your files below or click "Add files" to browse your computer. Click the Finish button when you're done</p>
        <p>For help go to http://helpme.help/</p>
    </div>

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
                
                <div class="dropzone-files files connectedSortable">
                    <#if documents.getFiles()?size == 0>
                        <div class="empty-message">Drag files here to upload</div>
                    </#if>
                    <#list documents.getFiles() as file>
                        <div id="documents-${file.id}" class="file">
                            <div class="file-options">
                                <button class="btn btn-xs btn-danger delete" disabled data-toggle="modal" data-target="#documentUploadModal">Delete</button>
                            </div>
                            <div class="filename">
                                <i class="fa fa-file-text-o"></i> <span class="filename-label">${file.name}</span>
                            </div>
                        </div>
                    </#list>
                </div>
                <div class="folder-options text-right">
                    <button class="btn btn-default fileinput-button" disabled>Add files &hellip;</button>
                    <button class="btn btn-warning finish" disabled data-toggle="modal" data-target="#documentUploadModal">Finish</button>
                </div>
            </div>
        </div>
    </div>
</section>
<#include "_modal.html.tpl" />