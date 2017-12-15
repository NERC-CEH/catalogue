<section class="section">
    <div class="container">
         <div class="intro">
         <h2>Deposit in progress</h2>
        </div>
        <div class="messages alert alert-info" role="alert">
            <div class="message loading">
                <span class="fa fa-refresh fa-spin"></span>
                <span>Loading please wait ...</span>
            </div>
        </div>
    </div>
</section>
<section class="section in-progress">
    <div class="container-fluid folders"> 
        <div class="documents folder">
            <h2 class="folder-name"><i class="fa fa-archive"></i> Dropbox</h2>
            <div class="files connectedSortable">
                <div class="empty-message"></div>
                <#list documents.getFiles() as file>
                    <div id="documents-${file.id}" class="file">
                        <div class="filename">
                            <i class="fa fa-file-text-o"></i> <span class="filename-label">${file.name}</span>
                        </div>
                    </div>
                </#list>
                <#list documents.getInvalid()?values as file>
                    <div id="documents-${file.id}" class="file file-invalid">
                        <div class="filename">
                            <i class="fa fa-file-text-o"></i> <span class="filename-label">${file.name}</span>
                        </div>
                        <div class="invalid-container">
                            <i class="fa fa-warning"></i> <span>${file.getLatestComment()}</span>
                            <div class="text-right">
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
                    </div>
                </#list>
            </div>
            <div class="folder-options text-right">
                <#if documents.getFiles()?size != 0>
                    <button class="btn btn-success move-to-datastore" disabled>Move all to datastore</button>
                </#if>
            </div>
        </div>
        <div class="plone folder">
            <h2 class="folder-name"> <i class="fa fa-files-o"></i> Metadata</h2>
            <div class="files connectedSortable">
                    <#if plone.getFiles()?size != 0 || plone.getInvalid()?size != 0>
                        <div class="empty-message"></div>
                    <#elseif documents.getFiles()?size == 0>
                        <div class="empty-message">No files in <span>Metadata</span> drag from <span>Documents</span></div>
                    <#else>
                        <div class="empty-message">No files in <span>Metadata</span> drag from <span>Datastore</span></div>
                    </#if>
                    <#list plone.getFiles() as file>
                        <div id="plone-${file.id}" class="file">
                            <div class="filename">
                                <i class="fa fa-file-text-o"></i> <span class="filename-label">${file.name}</span>
                            </div>
                        </div>
                    </#list>
                    <#list plone.getInvalid()?values as file>
                        <div id="plone-${file.id}" class="file file-invalid ">
                            <div class="filename">
                                <i class="fa fa-file-text-o"></i> <span class="filename-label">${file.name}</span>
                            </div>
                            <div class="invalid-container">
                                <i class="fa fa-warning"></i> <span>${file.getLatestComment()}</span>
                                <div class="text-right">
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
                        </div>
                    </#list>
            </div>
            <#if documents.getFiles()?size != 0 || documents.getInvalid()?size != 0>
                <div class="folder-options"></div>
            <#else>
                <div class="folder-options is-empty"></div>
            </#if>
        </div>
        <div class="datastore folder">
            <h2 class="folder-name"><i class="fa fa-lock"></i> Datastore
                <#if datastore.isZipped()>
                    <small class="zip" style="display: none"></small>
                    <small class="unzip">ZIPPED <i class="fa fa-file-archive-o"></i></small>
                <#else>
                    <small class="zip"></small>
                    <small class="unzip" style="display: none">ZIPPED <i class="fa fa-file-archive-o"></i></small>
                </#if>
            </h2>

            <div class="files connectedSortable ">
                <#if datastore.getFiles()?size != 0 || datastore.getInvalid()?size != 0>
                    <div class="empty-message"></div>
                <#elseif documents.getFiles()?size == 0>
                    <div class="empty-message">No files in <span>Datastore</span> drag from <span>Documents</span></div>
                <#else>
                    <div class="empty-message">No files in <span>Datastore</span> drag from <span>Metadata</span></div>
                </#if>
                <#list datastore.getFiles() as file>
                    <div id="datastore-${file.id}" class="file">
                        <div  class="filename">
                            <i class="fa fa-file-text-o"></i> <span class="filename-label">${file.name}</span>
                        </div >
                    </div>
                </#list>
                <#list datastore.getInvalid()?values as file>
                    <div id="datastore-${file.id}" class="file file-invalid">
                        <div  class="filename">
                            <i class="fa fa-file-text-o"></i> <span class="filename-label">${file.name}</span>
                        </div >
                        <div class="invalid-container">
                            <i class="fa fa-warning"></i> <span>${file.getLatestComment()}</span>
                            <div class="text-right">
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
                    </div>
                </#list>
            </div>
            <div class="folder-options text-right">
                <#if datastore.isZipped()>
                    <button class="btn btn-success zip" disabled style="display: none">Zip</button>
                    <button class="btn btn-success unzip" disabled>Unzip</button>
                <#else>
                    <button class="btn btn-success zip" disabled>Zip</button>
                    <button class="btn btn-success unzip" disabled style="display: none">Unzip</button>
                </#if>
                <#if (documents.getFiles()?size = 0 && documents.getInvalid()?size = 0) 
                && (datastore.getFiles()?size != 0 || datastore.getInvalid()?size != 0) >&nbsp;
                    <a class="btn btn-success downloadChecksum" href="data:text/csv;charset=utf-8,${csvList}" download="${guid}.csv">Download checksum report</a>
                </#if>
            </div>
        </div>
    </div>
</section>
<#include "_modal.html.tpl" />