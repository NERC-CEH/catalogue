<section class="section">
    <div class="intro">
        <p>Drag and drop your files below or click "Add files" to browse your computer.</p>
        <p>Click <b>Finish</b> when you're done</p>
        <p>For more help go to <a href="http://eidc.ceh.ac.uk/help/depositing/uploadData" target="_blank" rel="noopener">http://eidc.ceh.ac.uk/help/depositing/uploadData</a></p>
    </div>

    <div class="messages alert alert-info" role="alert">
        <div class="message loading">
            <span class="fas fa-sync fa-spin"></span>
            <span>Loading please wait ...</span>
        </div>
    </div>
</section>
<section class="section">
    <div class="container-fluid folders scheduled">
        <div class="row">
            <div class="documents folder">
                <div class="dropzone-files files connectedSortable">
                    <div class="empty-message">Please Wait</div>
                </div>
                <div class="folder-options text-right">
                    <button class="btn btn-default fileinput-button" disabled>Add files &hellip;</button>
                    <button class="btn btn-warning finish" disabled data-toggle="modal" data-target="#documentUploadModal">Finish</button>
                </div>
            </div>
        </div>
    </div>
</section>
<#include "_modal.ftl" />