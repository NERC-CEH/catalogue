<#if
datastore.getFiles()?size != 0 || documents.getFiles()?size != 0 || datastore.getInvalid()?values?size != 0 ||documents.getInvalid()?values?size != 0 || plone.getFiles()?size != 0 || plone.getInvalid()?values?size != 0 >

    <section class="section">
        <div class="intro">
            <h2>These are the data and metadata files that have been deposited.</h2>
            <p>We use MD5 checksums to verify data integrity and to ensure no errors occur during the files' transmission or storage. You can download a copy of the checksums for the data below.  For more information about checksums visit <a href="http://eidc.ceh.ac.uk/deposit/checksums" target="_blank">http://eidc.ceh.ac.uk/deposit/checksums/</a></p>
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
                <div class="folder clearfix">
                    <div class="folder-title">
                        <h2 class="folder-name">
                            <i class="fa fa-lock"></i> Data
                            <#if datastore.isZipped()>
                                <small class="zip" style="display: none"></small>
                                <small class="unzip">ZIPPED <i class="fa fa-file-archive-o"></i></small>
                            <#else>
                                <small class="zip"></small>
                                <small class="unzip" style="display: none">ZIPPED <i class="fa fa-file-archive-o"></i></small>
                            </#if>
                        </h2>
                    </div>
                    <div class="files">
                        <#if datastore.getFiles()?size == 0 && documents.getFiles()?size == 0 && datastore.getInvalid()?values?size == 0 && documents.getInvalid()?values?size == 0>
                            <div class="empty-message"><span>No files</span></div>
                        <#else>
                            <#list datastore.getInvalid()?values as file>
                                <div id="datastore-invalid-${file.id}" class="file b file-readonly is-inactive">
                                    <div class="filename">
                                        <i class="fa fa-file-text-o"></i> <span>${file.name}</span>
                                    </div>
                                </div>
                            </#list>
                            <#list datastore.getFiles() as file>
                                <div id="datastore-${file.id}" class="file file-readonly is-inactive">
                                    <div class="filename">
                                        <i class="fa fa-file-text-o"></i> <span>${file.name}</span>
                                        </div>
                                </div>
                            </#list>
                            <#list documents.getFiles() as file>
                                <div id="documents-${file.id}" class="file file-readonly is-inactive">
                                    <div class="filename">
                                        <i class="fa fa-file-text-o"></i> <span>${file.name}</span>
                                    </div>
                                </div>
                            </#list>
                            <#list documents.getInvalid()?values as file>
                                <div id="documents-invalid-${file.id}" class="file file-readonly is-inactive">
                                    <div class="filename">
                                        <i class="fa fa-file-text-o"></i> <span>${file.name}</span>
                                    </div>
                                </div>
                            </#list>
                            <div class="folder-options clearfix">
                                <a class="btn btn-success downloadChecksum" href="data:text/csv;charset=utf-8,${csvList}" download="${guid}.csv">Download checksum report</a>
                               <#if datastore.isZipped()>
                                    <button class="btn btn-success btn-zip zip" disabled style="display: none">Zip</button>
                                    <button class="btn btn-success btn-zip unzip" disabled>Unzip</button>
                                <#else>
                                    <button class="btn btn-success btn-zip zip" disabled>Zip</button>
                                    <button class="btn btn-success btn-zip unzip" disabled style="display: none">Unzip</button>
                                </#if>
                            </div>
                        </#if>
                    </div>
                </div>
                <#if plone.getFiles()?size != 0 || plone.getInvalid()?values?size != 0>
                    <div class="folder clearfix">
                        <div class="folder-title">
                            <h2 class="folder-name"><i class="fa fa-files-o"></i> Metadata</h2>
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
</#if>