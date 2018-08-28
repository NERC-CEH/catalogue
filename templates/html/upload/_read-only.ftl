<section class="section">
    <div class="intro">
        <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
            <#if issues[0].status == 'scheduled'>
                <div class="alert alert-info"><i class="fas fa-info-circle"></i> UPLOAD IN PROGRESS</div>
            <#elseif issues[0].status == 'in progress' >
                <div class="alert alert-info><i class="fas fa-info-circle"></i> You must <a href="./${id}/permission">amend permissions</a> to move files/fix problems</div>
            </#if>
        <#else>
            <p>We use MD5 checksums to verify data integrity and to ensure no errors occur during the files' transmission or storage. You can download a copy of the checksums for the data below.  For more information about checksums visit <a href="http://eidc.ceh.ac.uk/deposit/checksums" target="_blank" rel="noopener">http://eidc.ceh.ac.uk/deposit/checksums/</a></p>
        </#if>
    </div>
    <div class="messages alert alert-info" role="alert">
        <div class="message loading">
            <span class="fas fa-sync fa-spin"></span>
            <span>Loading please wait ...</span>
        </div>
    </div>
</section>

<section class="section">
    <div class="container-fluid folders read-only">
        <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
            <div class="folder clearfix documents">
                <div class="folder-title">
                    <h2 class="folder-name"><i class="fas fa-lock"></i> Dropbox</h2>
                </div>
                <table class='table' style='margin-bottom: 0'>
                    <thead>
                        <tr>
                            <th></th>
                            <th>Filename</th>
                            <th>Hash</th>
                            <th>Location</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody class='files-list'>
                    </tbody>
                </table>
            </div>
        </#if>
        <div class="folder clearfix datastore">
            <div class="folder-title">
                <h2 class="folder-name"><i class="fas fa-lock"></i> Data</h2>
            </div>
            <table class='table' style='margin-bottom: 0'>
                <thead>
                    <tr>
                        <th></th>
                        <th>Filename</th>
                        <th>Hash</th>
                        <th>Location</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody class='files-list'></tbody>
                <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
                    <tbody class='files-list-invalid'></tbody>
                </#if>
            </table>
        </div>
        <div class="folder clearfix plone">
            <div class="folder-title">
                <h2 class="folder-name"><i class="fas fa-lock"></i> Metadata</h2>
            </div>
            <table class='table' style='margin-bottom: 0'>
                <thead>
                    <tr>
                        <th></th>
                        <th>Filename</th>
                        <th>Hash</th>
                        <th>Location</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody class='files-list'></tbody>
                <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
                    <tbody class='files-list-invalid'></tbody>
                </#if>
            </table>
        </div>
    </div>
    <div class="folder-options text-right">
        <a class="btn btn-success downloadChecksum" href="get_checksum" download="checksums_${id}.csv">Download checksum report</a>
    </div>
</section>
