<section class="section">
    <p class="alert alert-success" role="alert">
        A list of documents and checksums
    </p>
    <div class="messages alert alert-info" role="alert">
        <div class="message loading">
            <span class="fa fa-refresh fa-spin"></span>
            <span>Loading please wait ...</span>
        </div>
    </div>
</section>
<section class="section">
    <div class="container-fluid folders read-only">
        <div class="row">
            <div class="col-md-12">
                <div class="folder">
                    <div class="folder-title">
                        <span class="folder-name">
                            <i class="fa fa-folder"></i> Documents
                        </span>
                    </div>
                    <div class="files">
                        <#if datastore.getFiles()?size == 0 && documents.getFiles()?size == 0 && datastore.getInvalid()?values?size == 0 && documents.getInvalid()?values?size == 0>
                            <div class="empty-message">No files</div>
                        <#else>
                            <div class="empty-message"></div>
                        </#if>
                        <#list datastore.getInvalid()?values as file>
                            <div id="datastore-invalid-${file.id}" class="file btn btn-primary file-readonly is-inactive">
                                <div class="filename">
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="fa fa-file-text-o"></i>
                                        </span>
                                        <input type="text" class="form-control filename-input" value="${file.name}" readonly>
                                    </div>
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="fa fa-hashtag"></i>
                                        </span>
                                        <input type="text" class="form-control filehash-input" value="${file.hash}" readonly>
                                    </div>
                                </div>
                            </div>
                        </#list>
                        <#list datastore.getFiles() as file>
                            <div id="datastore-${file.id}" class="file btn btn-primary file-readonly is-inactive">
                                <div class="filename">
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="fa fa-file-text-o"></i>
                                        </span>
                                        <input type="text" class="form-control filename-input" value="${file.name}" readonly>
                                    </div>
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="fa fa-hashtag"></i>
                                        </span>
                                        <input type="text" class="form-control filehash-input" value="${file.hash}" readonly>
                                    </div>
                                </div>
                            </div>
                        </#list>
                        <#list documents.getFiles() as file>
                            <div id="documents-${file.id}" class="file btn btn-primary file-readonly is-inactive">
                                <div class="filename">
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="fa fa-file-text-o"></i>
                                        </span>
                                        <input type="text" class="form-control filename-input" value="${file.name}" readonly>
                                    </div>
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="fa fa-hashtag"></i>
                                        </span>
                                        <input type="text" class="form-control filehash-input" value="${file.hash}" readonly>
                                    </div>
                                </div>
                            </div>
                        </#list>
                        <#list documents.getInvalid()?values as file>
                            <div id="documents-invalid-${file.id}" class="file btn btn-primary file-readonly is-inactive">
                                <div class="filename">
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="fa fa-file-text-o"></i>
                                        </span>
                                        <input type="text" class="form-control filename-input" value="${file.name}" readonly>
                                    </div>
                                    <div class="input-group">
                                        <span class="input-group-addon">
                                            <i class="fa fa-hashtag"></i>
                                        </span>
                                        <input type="text" class="form-control filehash-input" value="${file.hash}" readonly>
                                    </div>
                                </div>
                            </div>
                        </#list>
                    </div>
                    <div class="folder-options is-empty"></div>
                </div>
            </div>
        </div>
    </div>
</section>