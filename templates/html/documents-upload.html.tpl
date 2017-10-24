<#import "skeleton.html.tpl" as skeleton>
    <@skeleton.master title="Upload Files">
        <div id="documents-upload" class="container documents-upload">
            <section class="section">
                <h1 class="title">
                    <small class="title-type">${documentUpload.type}</small>
                    <span>${documentUpload.title}</span>
                </h1>
            </section>
            <section class="section">
                <#if canUpload && isScheduled>
                    <div class="alert alert-success" role="alert">Currently <b>Scheduled</b>. Drag files into <b>Documents</b> to upload.</div>
                <#elseif canUpload && isInProgress>
                    <div class="alert alert-success" role="alert">Currently <b>In Progress</b>. You can now move and zip files</div>
                <#elseif !canUpload>
                    <div class="alert alert-danger" role="alert">You do not have permissions to view this page</div>
                <#else>
                    <div class="alert alert-success" role="alert">All done, nothing to see here</div>
                </#if>
                <#if canUpload>
                    <div class="messages alert alert-info" role="alert">
                        <#if isScheduled>
                            <div class="message loading">
                                <span class="fa fa-refresh fa-spin"></span>
                                <span>Loading please wait ...</span>
                            </div>
                        </#if>
                    </div>
                </#if>

            </section>
            <#if canUpload && isScheduled>
                <section class="section">
                    <div class="container-fluid folders">
                        <div class="row">
                            <div class="documents folder">
                                <div class="folder-title">
                                    <span class="folder-name">
                                        <i class="fa fa-folder"></i> Documents
                                    </span>
                                </div>
                                <div class="dropzone-files files connectedSortable">
                                    <#if documentUpload.getFiles()?size == 0>
                                        <div class="ui-state-disabled empty-message">Drag files into <u>here</u> to upload</div>
                                    <#else>
                                        <div class="ui-state-disabled empty-message"></div>
                                    </#if>
                                    <#list documentUpload.getFiles() as file>
                                        <div id="${file.id}" class="file btn btn-primary">
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
            <#elseif canUpload && isInProgress>
                <div>dropbox, eidchub, plone, trash</div>
            </#if>
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
                    <button type="button" class="btn btn-default modal-dismiss" data-dismiss="modal"></button>
                    <button type="button" class="btn btn-danger modal-accept"  data-dismiss="modal"></button>
                </div>
                </div>
            </div>
        </div>
        <div class="navbar navbar-default document-upload-footer">
            <div class="container">
                <div class="navbar-right">
                    <a class="btn btn-default navbar-btn" href="/documents/${documentUpload.guid}">Return to metadata</a>
                </div>
            </div>
        </div>
    </@skeleton.master>