<section class="section">
    <div class="intro">
        <h2>Data files</h2>
        <p>These are the files that blah de blah de blah</p>
        <p>Something about checksums with link to help somewhere http://helpy.help.com/</p>
    </div>
    <div class="messages alert alert-info" role="alert">
        <div class="message loading">
            <span class="fa fa-refresh fa-spin"></span>
            <span>Loading please wait ...</span>
        </div>
    </div>
</section>

<section class="section">
    <div class="container-fluid folders read-only">
            <div class="folder">
                <div class="files">
                    <#if datastore.getFiles()?size == 0 && documents.getFiles()?size == 0 && datastore.getInvalid()?values?size == 0 && documents.getInvalid()?values?size == 0>
                        <div class="empty-message">No files</div>
                    <#else>
                        <#list datastore.getInvalid()?values as file>
                            <div id="datastore-invalid-${file.id}" class="file b file-readonly is-inactive">
                                <div class="filename">
                                    <i class="fa fa-file-text-o"></i> <span>${file.name}</span>
                                </div>
                                 <small class="filehash">{file.hash}</small>
                            </div>
                        </#list>
                        <#list datastore.getFiles() as file>
                            <div id="datastore-${file.id}" class="file file-readonly is-inactive">
                                <div class="filename">
                                    <i class="fa fa-file-text-o"></i> <span>${file.name}</span>
                                    </div>
                                    <small class="filehash">{file.hash}</small>
                            </div>
                        </#list>
                        <#list documents.getFiles() as file>
                            <div id="documents-${file.id}" class="file file-readonly is-inactive">
                                <div class="filename">
                                    <i class="fa fa-file-text-o"></i> <span>${file.name}</span>
                                </div>
                                <small class="filehash">${file.hash}</small>
                            </div>
                        </#list>
                        <#list documents.getInvalid()?values as file>
                            <div id="documents-invalid-${file.id}" class="file file-readonly is-inactive">
                                <div class="filename">
                                    <i class="fa fa-file-text-o"></i> <span>${file.name}</span>
                                </div>
                                 <small class="filehash">${file.hash}</small>
                            </div>
                        </#list>
                        <div class="folder-options">
                            <a class="btn btn-success download-files" href="data:text/csv;charset=utf-8,${csvList}" download="${guid}.csv">Download checksum report</a>
                        </div>
                    </#if>
                </div>
            </div>
            <#if plone.getFiles()?size != 0 || plone.getInvalid()?values?size != 0>
                <div class="folder">
                    <div class="folder-title">
                        <span class="folder-name">
                            <i class="fa fa-folder"></i> Metadata
                        </span>
                    </div>
                    <div class="files">
                        <div class="empty-message"></div>
                        <#list plone.getInvalid()?values as file>
                            <div id="plone-invalid-${file.id}" class="file file-readonly is-inactive">
                                <div class="filename">
                                    <i class="fa fa-file-text-o"></i> <span>{file.name}</span>
                                 </div>
                            </div>
                        </#list>
                        <#list plone.getFiles() as file>
                            <div id="plone-${file.id}" class="file file-readonly is-inactive">
                                <div class="filename">
                                    <i class="fa fa-file-text-o"></i> <span>${file.name}</span>
                                </div>
                            </div>
                        </#list>
                    </div>
                    <div class="folder-options is-empty"></div>
                </div>
            </#if>
    </div>
</section>