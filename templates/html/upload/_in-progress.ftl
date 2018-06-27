<p class="text-right"><a class="btn btn-default" href="./${id}/validate">VALIDATE</a></p>

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
                <div class="empty-message"></div>
                <#list uploadFiles['documents'].documents?values?sort_by('name') as file>
                    <div id="documents-${file.id}" class="file">
                        <div class="filename">
                            <i class="far fa-file-alt"></i> <span class="filename-label">${file.name}</span>
                        </div>
                    </div>
                </#list>
                <#list uploadFiles['documents'].invalid?values?sort_by('name') as file>
                    <div id="documents-${file.id}" class="file file-invalid">
                        <div class="filename">
                            <i class="far fa-file-alt"></i> <span class="filename-label">${file.name}</span>
                        </div>
                        <div class="invalid-container">
                            <i class="fas fa-exclamation-triangle"></i> <span>${file.type}</span>
                            <#if file.type == "INVALID_HASH" || file.type == "UNKNOWN_FILE">
                                <button class="btn btn-xs btn-success accept" disabled>Accept</button>
                            </#if>
                            <#if file.type == "MISSING_FILE">
                                <button class="btn btn-xs btn-danger ignore" disabled>Ignore</button>
                            </#if>
                            <#if file.type == "UNKNOWN_FILE">
                                <button class="btn btn-xs btn-danger delete" disabled data-toggle="modal" data-target="#documentUploadModal">Delete</button>
                            </#if>
                        </div>
                    </div>
                </#list>
            </div>
            <div class="folder-options text-right">
                <button class="btn btn-success move-to-datastore" disabled>Move all to datastore</button>
            </div>
        </div>
        <div class="plone folder">
            <h2 class="folder-name"> <i class="far fa-copy"></i> Metadata</h2>
            <div class="files connectedSortable">
                    <#if uploadFiles['plone'].documents?size != 0 || uploadFiles['plone'].invalid?size != 0>
                        <div class="empty-message"></div>
                    <#elseif uploadFiles['documents'].documents?size != 0 || uploadFiles['documents'].invalid?size != 0>
                        <div class="empty-message">No files in <span>Metadata</span> drag from <span>Documents</span></div>
                    <#else>
                        <div class="empty-message">No files in <span>Metadata</span> drag from <span>Datastore</span></div>
                    </#if>
                    <#list uploadFiles['plone'].documents?values?sort_by('name') as file>
                        <div id="plone-${file.id}" class="file">
                            <div class="filename">
                                <i class="far fa-file-alt"></i> <span class="filename-label">${file.name}</span>
                            </div>
                        </div>
                    </#list>
                    <#list uploadFiles['plone'].invalid?values?sort_by('name') as file>
                        <div id="plone-${file.id}" class="file file-invalid ">
                            <div class="filename">
                                <i class="far fa-file-alt"></i> <span class="filename-label">${file.name}</span>
                            </div>
                            <div class="invalid-container">
                                <i class="fas fa-exclamation-triangle"></i> <span>${file.type}</span>
                                <#if file.type == "INVALID_HASH" || file.type == "UNKNOWN_FILE">
                                    <button class="btn btn-xs btn-success accept" disabled>Accept</button>
                                </#if>
                                <#if file.type == "MISSING_FILE">
                                    <button class="btn btn-xs btn-danger ignore" disabled>Ignore</button>
                                </#if>
                                <#if file.type == "UNKNOWN_FILE">
                                    <button class="btn btn-xs btn-danger  delete" disabled data-toggle="modal" data-target="#documentUploadModal">Delete</button>
                                </#if>
                            </div>
                        </div>
                    </#list>
            </div>
        </div>
        <div class="datastore folder">
            <h2 class="folder-name"><i class="fas fa-lock"></i> Datastore
                <#if uploadFiles['datastore'].zipped>
                    <small class="zip" style="display: none"></small>
                    <small class="unzip">ZIPPED <i class="far fa-file-archive"></i></small>
                <#else>
                    <small class="zip"></small>
                    <small class="unzip" style="display: none">ZIPPED <i class="far fa-file-archive"></i></small>
                </#if>
            </h2>
            <div class="files connectedSortable ">
                    <#if uploadFiles['datastore'].documents?size != 0 || uploadFiles['datastore'].invalid?size != 0>
                        <div class="empty-message"></div>
                    <#elseif uploadFiles['documents'].documents?size != 0 || uploadFiles['documents'].invalid?size != 0>
                        <div class="empty-message">No files in <span>Metadata</span> drag from <span>Documents</span></div>
                    <#else>
                        <div class="empty-message">No files in <span>Metadata</span> drag from <span>Datastore</span></div>
                    </#if>
                <#list uploadFiles['datastore'].documents?values?sort_by('name') as file>
                    <div id="datastore-${file.id}" class="file">
                        <div  class="filename">
                            <i class="far fa-file-alt"></i> <span class="filename-label">${file.name}</span>
                        </div >
                    </div>
                </#list>
                <#list uploadFiles['datastore'].invalid?values?sort_by('name') as file>
                    <div id="datastore-${file.id}" class="file file-invalid">
                        <div  class="filename">
                            <i class="far fa-file-alt"></i> <span class="filename-label">${file.name}</span>
                        </div >
                        <div class="invalid-container">
                            <i class="fas fa-exclamation-triangle"></i> <span>${file.type}</span>
                            <#if file.type == "INVALID_HASH" || file.type == "UNKNOWN_FILE">
                                <button class="btn btn-xs btn-success accept" disabled>Accept</button>
                            </#if>
                            <#if file.type == "MISSING_FILE">
                                <button class="btn btn-xs btn-danger ignore" disabled>Ignore</button>
                            </#if>
                            <#if file.type == "UNKNOWN_FILE">
                                <button class="btn btn-xs btn-danger  delete" disabled data-toggle="modal" data-target="#documentUploadModal">Delete</button>
                            </#if>
                        </div>
                    </div>
                </#list>
            </div>
            <div class="folder-options text-right">
                <#if uploadFiles['datastore'].zipped>
                    <button class="btn btn-success zip" disabled style="display: none">Zip</button>
                    <button class="btn btn-success unzip" disabled>Unzip</button>
                <#else>
                    <button class="btn btn-success zip" disabled>Zip</button>
                    <button class="btn btn-success unzip" disabled style="display: none">Unzip</button>
                </#if>
                <a class="btn btn-success downloadChecksum" href="data:text/csv;charset=utf-8,${getCsv('datastore')}" download="checksums_${parentId}.csv">Download checksum report</a>
            </div>
        </div>
    </div>
</section>
<#include "_modal.ftl" />