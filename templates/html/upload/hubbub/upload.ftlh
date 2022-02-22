<#import "../../skeleton.ftlh" as skeleton>

<#assign uploadScripts>
    <script src="/static/scripts/hubbub-app.js"></script>
</#assign>

<@skeleton.master title="Data Transfer for: " + title scripts=uploadScripts catalogue=catalogues.defaultCatalogue()>
    <@documentContainer>
        <@titleHeading/>
        <@returnToDocument/>
        <#if !hasDataTransfer>
            <@noDataTransfer/>
        <#elseif isScheduled>
            <@documentUploadSection "Upload" "file-upload">
                <@dropZoneContainer/>
            </@documentUploadSection>
        </#if>
        <@documentUploadSection "Data" "archive">
            <@loadingContainer "data"/>
            <@loadMoreRow "data"/>
            <#if isAdmin && isInProgress>
                <@row>
                    <div class="col-md-3 col-md-offset-6">
                        <@moveAllDatastoreButton/>
                    </div>
                    <div class="col-md-3">
                        <@rescheduleButton/>
                    </div>
                </@row>
            <#elseif isAdmin && isOpen>
                <@row>
                    <div class="col-md-3 col-md-offset-6">
                        <@moveAllDatastoreButton/>
                    </div>
                    <div class="col-md-3">
                        <@scheduleButton/>
                    </div>
                </@row>
            <#elseif isAdmin>
                <@row>
                    <div class="col-md-3 col-md-offset-9">
                        <@moveAllDatastoreButton/>
                    </div>
                </@row>
            </#if>
        </@documentUploadSection>

        <#if isAdmin>
            <#assign metadataTitle>Metadata <small>Supporting Documents</small></#assign>
            <@documentUploadSection title=metadataTitle icon="copy" iconClass="far">
                <@loadingContainer "metadata"/>
                <@loadMoreRow "metadata"/>
            </@documentUploadSection>
            <@documentUploadSection "Datastore" "lock">
                <@loadingContainer "datastore"/>
                <@loadMoreRow "datastore"/>
            </@documentUploadSection>
        </#if>

        <@documentUploadSection "" "">
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
    <@documentUploadModal/>
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
            <i class="btn-icon fas fa-arrow-left"></i>
            <span>RETURN TO DOCUMENT: ${id}</span>
        </a>
    </div>
</#macro>

<#macro noDataTransfer>
    <div class="no-issue alert alert-danger">
        <b>ERROR</b><br>There is no Jira issue for this deposit
    </div>
</#macro>

<#macro documentUploadSection title icon iconClass="fas">
    <section class="document-upload-section">
        <#if title?has_content && icon?has_content>
            <div class="page-header">
                <h3><i class="btn-icon ${iconClass} fa-${icon}"></i> ${title}</h3>
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

<#macro loadMoreButton storage>
    <@button "View More" "load ${storage}" "angle-double-down"/>
</#macro>

<#macro scheduleButton>
    <@button text="Schedule" button="data-action schedule" icon="calendar-check" iconClass="far"/>
</#macro>

<#macro rescheduleButton>
    <@button "Reschedule" "data-action reschedule" "redo"/>
</#macro>

<#macro moveAllDatastoreButton>
    <@button "Move All Datastore" "data-action move-all" "level-down-alt"/>
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
        <i class="btn-icon fas fa-file-download"></i>
        <span>Download Checksum Report</span>
    </a>
</#macro>

<#macro data title files>
    <#list files>
        <script id="${title}-data" type="application/json">
        [<#items as file>
            {"bytes":${file.bytes?c},"hash":"${file.hash}","name":"${file.name}","path":"${file.path}","status":"${file.status}","time":${file.time?c}}<#sep>,</#sep>
        </#items>]
        </script>
    </#list>
</#macro>

<#macro documentUploadModal>
    <div class="modal fade" id="documentUploadModal" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title"></h4>
                </div>
                <div class="modal-body"></div>
                <div class="modal-footer">
                    <button class="btn btn-default modal-dismiss" data-dismiss="modal">NO</button>
                    <button class="btn btn-danger modal-accept"  data-dismiss="modal">YES</button>
                </div>
            </div>
        </div>
    </div>
</#macro>
