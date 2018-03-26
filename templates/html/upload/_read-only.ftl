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
            <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
                <#if issues[0].status == 'scheduled'>
                    <div class="alert alert-info"><i class="fa fa-info-circle"></i> UPLOAD IN PROGRESS</div>
                <#elseif issues[0].status == 'in progress' >
                    <div class="alert alert-info><i class="fa fa-info-circle"></i> You must <a href="./${parentId}/permission">amend permissions</a> to move files/fix problems</div>
                </#if>
            <#else>
                <p>We use MD5 checksums to verify data integrity and to ensure no errors occur during the files' transmission or storage. You can download a copy of the checksums for the data below.  For more information about checksums visit <a href="http://eidc.ceh.ac.uk/deposit/checksums" target="_blank" rel="noopener">http://eidc.ceh.ac.uk/deposit/checksums/</a></p>
            </#if>
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
            <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN") &&  uploadFiles['documents'].documents?size != 0 || uploadFiles['documents'].invalid?size != 0 >
            <div class="folder clearfix">
                <div class="folder-title">
                    <h2 class="folder-name">
                        <i class="fa fa-lock"></i> Dropbox
                    </h2>
                </div>
                <div class="files">
                    <div class="file fileHeader">
                        <div class="fileicon"></div>
                        <div class="filename">Filename</div>
                        <div class="filehash">Hash</div>
                        <div class="filelocation">Location</div>
                        <div class="filevalidity"></div>
                    </div>
                   
                    <#list uploadFiles['documents'].invalid?values?sort_by('name') + uploadFiles['documents'].documents?values?sort_by('name') as document>
                        ${displayFile(document)}
                    </#list>

                </div>
            </div>
            </#if>

            <#if uploadFiles['datastore'].documents?size != 0 || uploadFiles['datastore'].invalid?size != 0 >
            <div class="folder clearfix">
                <div class="folder-title">
                    <h2 class="folder-name">
                        <i class="fa fa-lock"></i> Data
                    </h2>
                </div>
                <div class="files">
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
                </div>
            </div>
            </#if>

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
        <div class="folder-options text-right">
            <a class="btn btn-success downloadChecksum" href="data:text/csv;charset=utf-8,${getCsv('documents', 'datastore')}" download="checksums_${parentId}.csv">Download checksum report</a>
        </div>

    </section>
<#elseif permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
    <div class="alert alert-info"><i class="fa fa-info-circle"></i> There are currently no files associated with this resource</div>
</#if>