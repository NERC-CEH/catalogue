<section class="section">
    <div class="container">
         <div class="intro">
         <p>
            <b>In Progress</b>
            <#if documents.getFiles()?size != 0 || documents.getInvalid()?size != 0>
                <br />Move file from <u>Documents</u> to <u>Datastore</u> and <u>Plone</u>
            </#if>
            <br />You can move files between <u>Datastore</u> and <u>Plone</u>
        </p>
        </div>
        <div class="messages alert alert-info" role="alert">
            <div class="message loading">
                <span class="fa fa-refresh fa-spin"></span>
                <span>Loading please wait ...</span>
            </div>
        </div>
    </div>
</section>
<section class="section">
    <div class="container-fluid folders in-progress">
        <div class="row">
            <#if documents.getFiles()?size != 0 || documents.getInvalid()?size != 0>
                <div class="col-md-6">
                    <div class="documents folder">
                        <div class="folder-title">
                            <span class="folder-name">
                                <i class="fa fa-folder"></i> Documents
                            </span>
                        </div>
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
                            <button class="btn btn-success move-to-datastore" disabled>Move to Datastore</button>
                        </div>
                    </div>
                </div>
            <div class="col-md-6">
            <#else>
            <div class="col-md-12">
            </#if>
                <div class="plone folder">
                    <div class="folder-title">
                        <span class="folder-name">
                            <i class="fa fa-database"></i> Plone
                        </span>
                    </div>
                    <div class="files connectedSortable">
                            <#if plone.getFiles()?size != 0 || plone.getInvalid()?size != 0>
                                <div class="empty-message"></div>
                            <#elseif documents.getFiles()?size == 0>
                                <div class="empty-message">No files in <u>Plone</u> drag from <u>Documents</u></div>
                            <#else>
                                <div class="empty-message">No files in <u>Plone</u> drag from <u>Datastore</u></div>
                            </#if>
                            <#list plone.getFiles() as file>
                                <div id="plone-${file.id}" class="file">
                                    <div class="filename">
                                        <i class="fa fa-file-text-o"></i> <span class="filename-label">${file.name}</span>
                                    </div>
                                </div>
                            </#list>
                            <#list plone.getInvalid()?values as file>
                                <div id="plone-${file.id}" class="file file-invalid btn btn-primary">
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
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div class="datastore folder">
                    <div class="folder-title">
                        <span class="folder-name">
                            <i class="fa fa-archive"></i> Datastore
                        </span>
                    </div>
                    <div class="files connectedSortable">
                            <#if datastore.getFiles()?size != 0 || datastore.getInvalid()?size != 0>
                                <div class="empty-message"></div>
                            <#elseif documents.getFiles()?size == 0>
                                <div class="empty-message">No files in <u>Datastore</u> drag from <u>Documents</u></div>
                            <#else>
                                <div class="empty-message">No files in <u>Datastore</u> drag from <u>Plone</u></div>
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
                            <button class="btn btn-success zip" disabled style="display: none" >Zip</button>
                            <button class="btn btn-success unzip" disabled>Unzip</button>
                        <#else>
                            <button class="btn btn-success zip" disabled>Zip</button>
                            <button class="btn btn-success unzip" disabled style="display: none">Unzip</button>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>
<#include "_modal.html.tpl" />