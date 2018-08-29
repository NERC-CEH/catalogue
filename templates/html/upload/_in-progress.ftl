<section class="section">
    <div class="container">
        <div class="messages alert alert-info" role="alert">
            <div class="message loading">
                <span class="fas fa-sync fa-spin"></span>
                <span>Loading please wait ...</span>
            </div>
        </div>
    </div>
</section>
<section class="section in-progress">
    <div class="container-fluid folders"> 
        <div class="documents folder">
            <h2 class="folder-name"><i class="fas fa-archive"></i> Dropbox</h2>
            <div class="files connectedSortable">
                <div class="empty-message">Please Wait</div>
            </div>
            <div class="folder-options text-right">
                <button class="btn btn-success move-to-datastore" disabled>Move all to datastore</button>
            </div>
        </div>
        <div class="plone folder">
            <h2 class="folder-name"> <i class="far fa-copy"></i> Metadata</h2>
            <div class="files connectedSortable">
                    <div class="empty-message">Please Wait</div>
            </div>
        </div>
        <div class="datastore folder">
            <h2 class="folder-name"><i class="fas fa-lock"></i> Datastore
                <#--  <#if uploadFiles['datastore'].zipped>
                    <small class="zip" style="display: none"></small>
                    <small class="unzip">ZIPPED <i class="far fa-file-archive"></i></small>
                <#else>
                    <small class="zip"></small>
                    <small class="unzip" style="display: none">ZIPPED <i class="far fa-file-archive"></i></small>
                </#if>  -->
            </h2>
            <div class="files connectedSortable ">
                <div class="empty-message">Please Wait</div>
            </div>
            <div class="folder-options text-right">
                <button class="btn btn-success zip" disabled>Zip</button>
                <button class="btn btn-success unzip" disabled style="display: none">Unzip</button>
                <a class="btn btn-success downloadChecksum" href="get_checksum" download="checksums_${id}.csv" disabled>Download checksum report</a>
            </div>
        </div>
    </div>
</section>
<#include "_modal.ftl" />