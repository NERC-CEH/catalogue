<#if
    uploadFiles['documents'].documents?size != 0 ||
    uploadFiles['documents'].invalid?size != 0 ||

    uploadFiles['datastore'].documents?size != 0 ||
    uploadFiles['datastore'].invalid?size != 0 ||

    uploadFiles['plone'].documents?size != 0 ||
    uploadFiles['plone'].invalid?size != 0
>
<section class="section">
    <div class="intro">
        <h2>These are the data and metadata files that have been deposited.</h2>
        <p>We use MD5 checksums to verify data integrity and to ensure no errors occur during the files' transmission or storage. You can download a copy of the checksums for the data below.  For more information about checksums visit <a href="http://eidc.ceh.ac.uk/deposit/checksums" target="_blank" rel="noopener">http://eidc.ceh.ac.uk/deposit/checksums/</a></p>
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
                        </h2>
                    </div>
                    <div class="files">
                        <#if uploadFiles['datastore'].documents?size == 0 && uploadFiles['documents'].documents?size == 0 && uploadFiles['datastore'].invalid?size == 0 && uploadFiles['documents'].documents?size == 0>
                            <div class="empty-message"><span>No files</span></div>
                        <#else>
                            <#list uploadFiles['datastore'].invalid?values as document>
                                <div class="file file-readonly is-inactive">
                                    <div class="filename">
                                        <i class="fa fa-file-<#if document.name?ends_with('.zip')>archive<#else>text</#if>-o"></i> <span>${document.name}</span>
                                    </div>
                                </div>
                            </#list>
                            <#list uploadFiles['datastore'].documents?values as document>
                                <div class="file file-readonly is-inactive">
                                    <div class="filename">
                                        <i class="fa fa-file-<#if document.name?ends_with('.zip')>archive<#else>text</#if>-o"></i> <span>${document.name}</span>
                                    </div>
                                </div>
                            </#list>
                            <#list uploadFiles['documents'].invalid?values as document>
                                <div class="file file-readonly is-inactive">
                                    <div class="filename">
                                        <i class="fa fa-file-<#if document.name?ends_with('.zip')>archive<#else>text</#if>-o"></i> <span>${document.name}</span>
                                    </div>
                                </div>
                            </#list>
                            <#list uploadFiles['documents'].documents?values as document>
                                <div class="file file-readonly is-inactive">
                                    <div class="filename">
                                        <i class="fa fa-file-<#if document.name?ends_with('.zip')>archive<#else>text</#if>-o"></i> <span>${document.name}</span>
                                    </div>
                                </div>
                            </#list>
                        </#if>
                    </div>
                    <div class="folder-options text-right">
                        <a class="btn btn-success downloadChecksum" href="data:text/csv;charset=utf-8,${getCsv('documents', 'datastore')}" download="${parentId}.csv">Download checksum report</a>
                    </div>
                </div>
                <#if uploadFiles['plone'].documents?size != 0 || uploadFiles['plone'].invalid?values?size != 0>
                    <div class="folder clearfix">
                        <div class="folder-title">
                            <h2 class="folder-name"><i class="fa fa-files-o"></i> Metadata</h2>
                        </div>
                        <div class="files">
                            <div class="empty-message"></div>
                            <#list uploadFiles['plone'].invalid?values as document>
                                <div class="file file-readonly is-inactive">
                                    <div class="filename">
                                        <i class="fa fa-file-<#if document.name?ends_with('.zip')>archive<#else>text</#if>-o"></i> <span>${document.name}</span>
                                    </div>
                                </div>
                            </#list>
                            <#list uploadFiles['plone'].documents?values as document>
                                <div class="file file-readonly is-inactive">
                                    <div class="filename">
                                        <i class="fa fa-file-<#if document.name?ends_with('.zip')>archive<#else>text</#if>-o"></i> <span>${document.name}</span>
                                    </div>
                                </div>
                            </#list>
                        </div>
                    </div>
                </#if>
        </div>
    </section>

</#if>
