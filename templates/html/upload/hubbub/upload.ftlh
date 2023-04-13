<#import "../../skeleton.ftlh" as skeleton>

<@skeleton.master title="Data Transfer for: " + title scripts=uploadScripts catalogue=catalogues.defaultCatalogue()>
    <@documentContainer>
        <@titleHeading/>
        <@returnToDocument/>
        <#if !hasDataTransfer>
            <@noDataTransfer/>
        <#elseif isScheduled>
            <@documentUploadSection "Upload" "" "file-upload">
                <@dropZoneContainer/>
            </@documentUploadSection>
        </#if>
        <@documentUploadSection "Data" "dropbox" "archive">
            <@loadingContainer "data"/>
            <@loadMoreRow "data"/>
            <#if isAdmin && isInProgress>
                <@row>
                    <div class="col-md-3">
                        <@moveAllDatastoreButton/>
                    </div>
                    <div class="col-md-3">
                        <@rescheduleButton/>
                    </div>
                    <div class="col-md-3">
                        <@hashDropboxButton/>
                    </div>
                    <div class="col-md-3">
                        <@registerDropboxButton/>
                    </div>
                </@row>
            <#elseif isAdmin && isOpen>
                <@row>
                    <div class="col-md-3">
                        <@moveAllDatastoreButton/>
                    </div>
                    <div class="col-md-3">
                        <@scheduleButton/>
                    </div>
                    <div class="col-md-3">
                        <@hashDropboxButton/>
                    </div>
                    <div class="col-md-3">
                        <@registerDropboxButton/>
                    </div>
                </@row>
            <#elseif isAdmin>
                <@row>
                    <div class="col-md-3 col-md-offset-3">
                        <@moveAllDatastoreButton/>
                    </div>
                    <div class="col-md-3">
                        <@hashDropboxButton/>
                    </div>
                    <div class="col-md-3">
                        <@registerDropboxButton/>
                    </div>
                </@row>
            </#if>
        </@documentUploadSection>

        <#if isAdmin>
            <#assign metadataTitle>Metadata <small>Supporting Documents</small></#assign>
            <@documentUploadSection title=metadataTitle name="metadata" icon="copy" iconClass="far">
                <@loadingContainer "metadata"/>
                <@loadMoreRow "metadata"/>
            </@documentUploadSection>
            <@documentUploadSection "Datastore" "datastore" "lock">
                <@loadingContainer "datastore"/>
                <@loadMoreRow "datastore"/>
            </@documentUploadSection>
        </#if>

        <@documentUploadSection "" "" "">
            <@row>
                <#if isAdmin>
                    <div class="col-md-6 col-md-offset-6 file-actions">
                        <@validateAllButton/>
                        <@downloadChecksumButton/>
                    </div>
                <#else>
                    <div class="col-md-3 col-md-offset-9">
                        <@downloadChecksumButton/>
                    </div>
                </#if>
            </@row>
        </@documentUploadSection>
    </@documentContainer>
    <#if datastore??>
        <@data "datastore" datastore/>
    </#if>
    <#if dropbox??>
        <@data "dropbox" dropbox/>
    </#if>
    <#if metadata??>
        <@data "metadata" metadata/>
    </#if>
</@skeleton.master>

<#macro documentContainer>
    <div class="container is-<#if isAdmin>admin<#else>non-admin</#if>" id="document-upload" data-guid='${id}'>
        <div class="container-fluid document-upload">
            <#nested>
        </div>
    </div>
</#macro>

<#macro titleHeading>
    <div class="page-header">
        <h1>${title}</h1>
    </div>
</#macro>

<#macro returnToDocument>
    <div class="return">
        <a href="/documents/${id}">
            <i class="btn-icon fa-solid fa-arrow-left"></i>
            <span>RETURN TO DOCUMENT: ${id}</span>
        </a>
    </div>
</#macro>

<#macro noDataTransfer>
    <div class="no-issue alert alert-danger">
        <b>ERROR</b><br>There is no Jira issue for this deposit
    </div>
</#macro>

<#macro documentUploadSection title name icon iconClass="fas">
    <section class="document-upload-section ${name}">
        <#if title?has_content && icon?has_content>
            <div class="page-header">
                <h3><i class="btn-icon ${iconClass} fa-${icon}"></i> ${title} <small class="total-files"></small></h3>
            </div>
        </#if>
        <#nested>
    </section>
</#macro>

<#macro dropZoneContainer>
    <div class='dropzone-container'>
        <div class="dropzone-files">
            <@row "file-head">
                <div class="col-md-7">
                    <@row>
                        <div class="col-md-6"><b>Name</b></div>
                        <div class="col-md-2"><b>Size</b></div>
                        <div class="col-md-4"><b>Status</b></div>
                    </@row>
                </div>
                <div class="col-md-5 file-head-action">
                    <b>Actions</b>
                </div>
            </@row>
        </div>
        <@row>
            <div class="col-md-2 col-md-offset-7">
                <@addFilesButton/>
            </div>
            <div class="col-md-3">
                <@finishUploadingButton/>
            </div>
        </@row>
        <@row>
            <div class="col-md-2 col-md-offset-7">
                <p>Maximum individual file size is ${maxFileSize}</p>
            </div>
        </@row>
    </div>
</#macro>

<#macro loadingContainer name>
    <div class="${name}-files">
        <h3 class='no-documents text-center'>NO FILES IN ${name?upper_case}</h3>
    </div>
</#macro>

<#macro row classes="">
    <div class="row ${classes}">
        <#nested>
    </div>
</#macro>

<#macro loadMoreRow storage>
    <@row>
        <div class="col-md-2 col-md-offset-10">
            <@loadMoreButton storage/>
        </div>
    </@row>
</#macro>

<#macro button text button icon btnClass="success" iconClass="fas">
    <button class="btn btn-${btnClass} ${button}">
        <i class="btn-icon ${iconClass} fa-${icon}"></i>
        <span>${text}</span>
    </button>
</#macro>

<#macro addFilesButton>
    <@button "Add Files…" "fileinput-button" "file-upload"/>
</#macro>

<#macro finishUploadingButton>
    <@button "Finish Uploading" "finish" "check" "primary"/>
</#macro>

<#macro hashDropboxButton>
    <@button "Generate checksums" "data-action hash-dropbox" "wave-square"/>
</#macro>

<#macro loadMoreButton storage>
    <@button "View More" "load ${storage}" "angle-double-down"/>
</#macro>

<#macro registerDropboxButton>
    <@button "Register files" "data-action register-dropbox" "folder-plus"/>
</#macro>

<#macro scheduleButton>
    <@button text="Schedule" button="data-action schedule" icon="calendar-check" iconClass="far"/>
</#macro>

<#macro rescheduleButton>
    <@button "Reschedule" "data-action reschedule" "redo"/>
</#macro>

<#macro moveAllDatastoreButton>
    <@button "Move files to datastore" "data-action move-all" "level-down-alt"/>
</#macro>

<#macro validateAllButton>
    <@button "Validate All" "file-action validate-all" "check" "primary"/>
</#macro>

<#macro downloadChecksumButton>
    <a
        class="btn btn-success downloadChecksum"
        href="/upload/${id}?format=csv"
        download="checksums_${id}.csv"
    >
        <i class="btn-icon fa-regular fa-file-code"></i>
        <span>Download Checksum Report</span>
    </a>
</#macro>

<#macro data title files>
    <script id="${title}-data" type="application/json">
        ${files?no_esc}
    </script>
</#macro>
