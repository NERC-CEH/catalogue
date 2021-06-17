<#import "../../skeleton.ftlh" as skeleton>

<@skeleton.master title="Data Transfer for: " + title>
    <@documentContainer>
        <@returnToDocument/>
        <#if !hasDataTransfer>
            <@noDataTransfer/>
        <#elseif isScheduled>
            <@documentUploadSection "Upload" "file-upload">
                <@dropZoneContainer/>
            </@documentUploadSection>
        </#if>
        <@documentUploadSection "Data" "archive">
            <@pagination "documents"/>
            <@loadingContainer "documents"/>
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
                <@pagination "supporting-documents"/>
                <@loadingContainer "supporting-documents"/>
            </@documentUploadSection>
            <@documentUploadSection "Datastore" "lock">
                <@pagination "datastore"/>
                <@loadingContainer "datastore"/>
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
    <@documentUploadModal/>
</@skeleton.master>

<#macro documentContainer>
    <div class="container is-<#if isAdmin>admin<#else>non-admin</#if>" id="document-upload" data-guid='${id}'>
        <div class="container-fluid document-upload <#if isScheduled>is-scheduled</#if>">
            <#nested>
        </div>
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
    </div>
</#macro>

<#macro pagination pagId>
    <div id="pag-${pagId}">
        <div class="pag">
            <div class="pag-pages">
                <div class="pag-number input-group">
                    <input type="text" class="form-control pag-current">
                    <span class="input-group-addon pag-count">/ 1</span>
                </div>
                <div role="group" class="btn-group">
                    <button class="btn btn-success pag-previous">
                        <i class="fas fa-chevron-left"></i>
                    </button>
                    <button class="btn btn-success pag-next">
                        <i class="fas fa-chevron-right"></i>
                    </button>
                </div>
            </div>
            <p class="pag-per-page">
                <i class="fas fa-spinner fa-spin-2x" style='visibility: hidden'></i>
                <span>20 Files Per Page</span>
            </p>
        </div>
    </div>
</#macro>

<#macro loadingContainer name>
    <div class="row loading text-center">
        <h3><i class="fas fa-spinner fa-spin-2x"></i> LOADING</h3>
    </div>
    <div class="${name}-files"></div>
</#macro>

<#macro row classes="">
    <div class="row ${classes}">
        <#nested>
    </div>
</#macro>

<#macro button text button icon btnClass="success" iconClass="fas">
    <button class="btn btn-${btnClass} ${button}">
        <i class="btn-icon ${iconClass} fa-${icon}"></i>
        <span>${text}</span>
    </button>
</#macro>

<#macro addFilesButton>
    <@button "Add FILES ..." "fileinput-button" "file-upload"/>
</#macro>

<#macro finishUploadingButton>
    <@button "FINISH UPLOADING" "finish" "check" "primary"/>
</#macro>

<#macro scheduleButton>
    <@button text="SCHEDULE" button="data-action schedule" icon="calendar-check" iconClass="far"/>
</#macro>

<#macro rescheduleButton>
    <@button "RESCHEDULE" "data-action reschedule" "redo"/>
</#macro>

<#macro moveAllDatastoreButton>
    <@button "MOVE ALL DATASTORE" "data-action move-all" "level-down-alt"/>
</#macro>

<#macro validateAllButton>
    <@button "VALIDATE ALL" "file-action validate-all" "check" "primary"/>
</#macro>

<#macro downloadChecksumButton>
    <@button "DOWNLOAD CHECKSUM REPORT" "file-action downloadChecksum" "file-download"/>
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
