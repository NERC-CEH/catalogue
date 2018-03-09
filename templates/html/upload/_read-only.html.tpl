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
        <p>We use MD5 checksums to verify data integrity and to ensure no errors occur during the files' transmission or storage. You can download a copy of the checksums for the data below.  For more information about checksums visit <a href="http://eidc.ceh.ac.uk/deposit/checksums" target="_blank" rel="noopener">http://eidc.ceh.ac.uk/deposit/checksums/</a></p>
    </div>
    <div class="messages alert alert-info" role="alert">
        <div class="message loading">
            <span class="fa fa-refresh fa-spin"></span>
            <span>Loading please wait ...</span>
        </div>
    </div>
</section>


<#function displayFile document invalid=false>
  <#local icon = "text" problem = "" invalidclass="">
  
  <#if document.name?ends_with(".zip")>
    <#local icon = "archive">
  </#if>

  <#if invalid = true >
    <#local problem = "<b class='text-red'><i class='fa fa-exclamation-triangle'></i> INVALID</b>" invalidclass="file-invalid">
  </#if>

  <#local fileInfo = "<div id='" + document.id + "' class='file ${invalidclass} file-readonly is-inactive'>">
  <#local fileInfo += "<div class='fileicon'><i class='fa fa-file-" + icon +"-o'></i></div>">
  <#local fileInfo += "<div class='filename'>" + document.name + "</div>">
  <#local fileInfo += "<div class='filehash'>" + document.hash + "</div>">
  <#local fileInfo += "<div class='filelocation'>" + document.physicalLocation + "</div>">
  <#local fileInfo += "<div class='filevalidity'>" + problem + "</div>">
  <#local fileInfo += "</div>">
  <#return fileInfo>
</#function>

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
                        <div class="file fileHeader">
                            <div class="fileicon"></div>
                            <div class="filename">Filename</div>
                            <div class="filehash">Hash</div>
                            <div class="filelocation">Location</div>
                            <div class="filevalidity"></div>
                        </div>
                            <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
                                <#list uploadFiles['datastore'].invalid?values?sort_by('name') as document>
                                    ${displayFile(document, true)}
                                </#list>
                            </#if>
                            <#list uploadFiles['datastore'].documents?values?sort_by('name') as document>
                                ${displayFile(document)}
                            </#list>
                            <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
                                <#list uploadFiles['documents'].invalid?values?sort_by('name') as document>
                                    ${displayFile(document, true)}
                                </#list>
                            </#if>
                            <#list uploadFiles['documents'].documents?values?sort_by('name') as document>
                                ${displayFile(document)}
                            </#list>
                        </#if>
                    </div>
                    <div class="folder-options text-right">
                        <a class="btn btn-success downloadChecksum" href="data:text/csv;charset=utf-8,${getCsv('documents', 'datastore')}" download="${parentId}.csv">Download checksum report</a>
                    </div>
                </div>
                <#if uploadFiles['plone'].documents?size != 0 || uploadFiles['plone'].invalid?values?sort_by('name')?size != 0>
                    <div class="folder clearfix">
                        <div class="folder-title">
                            <h2 class="folder-name"><i class="fa fa-files-o"></i> Metadata</h2>
                        </div>
                        <div class="files">
                            <div class="file fileHeader">
                                <div class="fileicon"></div>
                                <div class="filename">Filename</div>
                                <div class="filehash">Hash</div>
                                <div class="filelocation">Location</div>
                                <div class="filevalidity"></div>
                            </div>
                            <div class="empty-message"></div>
                             <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
                                <#list uploadFiles['plone'].invalid?values?sort_by('name') as document>
                                    ${displayFile(document, true)}
                                </#list>
                            </#if>
                            <#list uploadFiles['plone'].documents?values?sort_by('name') as document>
                                ${displayFile(document)}
                            </#list>
                        </div>
                    </div>
                </#if>
        </div>
    </section>
</#if>