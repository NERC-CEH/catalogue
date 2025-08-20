<#import "../../skeleton.ftlh" as skeleton>

<@skeleton.master title="Data Transfer for: " + title scripts=uploadScripts catalogue=catalogues.defaultCatalogue()>
    <@documentContainer>
        <@titleHeading/>
        <@returnToDocument/>
        <#if !hasDataTransfer>
            <@noDataTransfer/>
        <#elseif isScheduled>
            <@documentUploadSection "Upload" "upload" "file-upload">
                <@dropZoneContainer/>
            </@documentUploadSection>
        </#if>

        <@documentUploadSection "Arrivals" "dropbox" "archive">
            <@loadingContainer "data"/>
            <@loadMoreRow "data"/>

            <#if isAdmin>
             <@row>
              <div class="d-inline-flex gap-2 justify-content-center flex-wrap">
                  <@moveAllDatastoreButton/>
                  <@hashDropboxButton/>
                </div>
              </@row>
            </#if>

        </@documentUploadSection>

        <#if isAdmin>
           <@row>
              <div class="d-inline-flex gap-2 justify-content-center flex-wrap">
                  <@registerDropboxButton/>
                  <#if isInProgress>
                    <@rescheduleButton/>
                  <#elseif isOpen>
                    <@scheduleButton/>
                  </#if>
              </div>
            </@row>

            <#assign metadataTitle>Supporting documents</#assign>
            <@documentUploadSection title=metadataTitle name="metadata" icon="file-word" iconClass="fa-solid">
                <@loadingContainer "metadata"/>
                <@loadMoreRow "metadata"/>
            </@documentUploadSection>

            <@documentUploadSection "Datastore" "datastore" "lock">
                <@loadingContainer "datastore"/>
                <@loadMoreRow "datastore"/>
            </@documentUploadSection>
        </#if>


        <@row>
            <#if isAdmin>
                <div class="d-inline-flex gap-2 justify-content-center flex-wrap file-actions">
                    <@validateAllButton/>
                    <@downloadChecksumButton/>
                </div>
            <#else>
                <div class="d-inline-flex gap-2">
                    <@downloadChecksumButton/>
                </div>
            </#if>
        </@row>

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
        <div class="container document-upload">
            <#nested>
        </div>
    </div>
</#macro>

<#macro titleHeading>
    <div class="page-header">
        ${title}
    </div>
</#macro>

<#macro returnToDocument>
    <div class="return">
        <a href="/documents/${id}">
            <i class="btn-icon fa-fw fa-solid fa-arrow-left"></i>
            Back to catalogue record: ${id}
        </a>
    </div>
</#macro>

<#macro noDataTransfer>
    <div class="no-issue alert alert-danger">
        <b>ERROR</b><br>There is no Jira issue for this deposit
    </div>
</#macro>

<#macro documentUploadSection title name icon iconClass="fa-solid">
    <section class="document-upload-section ${name}">
        <#if title?has_content && icon?has_content>
            <div class="section-header">
                <i class="btn-icon fa-fw ${iconClass} fa-${icon}"></i> ${title} <span class="total-files"></span>
            </div>
        </#if>
        <#nested>
    </section>
</#macro>

<#macro dropZoneContainer>
    <div class='dropzone-container'>
        <div class="dropzone-files">
            <div class="p-3 fs-4 text-center">Drag and drop files/folders here or click the <b>add files</b> button</div>
        </div>
        <@row "px-3">
            <div class="p-3 text-center text-body-tertiary">
              <i class="fa-solid fa-fw fa-info-circle"></i> Maximum individual file size is ${maxFileSize}
            </div>
            <@addFilesButton/>
        </@row>
        <@row "px-3">
            <@finishUploadingButton/>
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

<#macro loadMoreRow storage> <#--hide -->
    <@row "mb-3">
      <div class="d-inline-flex justify-content-end">
          <@loadMoreButton storage/>
      </div>
    </@row>
</#macro>

<#macro button text button icon iconClass="fa-solid" btnClass="btn-outline-dark">
    <button class="btn btn-uploader ${btnClass} ${button}">
        <i class="btn-icon fa-fw fa-${icon} ${iconClass}"></i>
        <span>${text}</span>
    </button>
</#macro>

<#macro addFilesButton>
    <@button text="Add files" button="fileinput-button" icon="file-upload"/>
</#macro>

<#macro finishUploadingButton>
    <@button text="Finish uploading" button="finish" icon="check"/>
</#macro>

<#macro hashDropboxButton>
    <@button text="Generate checksums" button="data-action hash-dropbox" icon="wave-square"/>
</#macro>

<#macro loadMoreButton storage>
    <@button text="Load more to table" button="load ${storage}" icon="download"/>
</#macro>

<#macro registerDropboxButton>
    <@button text="Register files" button="data-action register-dropbox" icon="folder-plus"/>
</#macro>

<#macro scheduleButton>
    <@button text="Schedule" button="data-action schedule" icon="calendar-check" iconClass="fa-regular"/>
</#macro>

<#macro rescheduleButton>
    <@button text="Reschedule" button="data-action reschedule" icon="redo"/>
</#macro>

<#macro moveAllDatastoreButton>
    <@button text="Move files to datastore" button="data-action move-all" icon="level-down-alt"/>
</#macro>

<#macro validateAllButton>
    <@button text="Validate all" button="file-action validate-all" icon="check"/>
</#macro>

<#macro downloadChecksumButton>
    <a
        class="btn btn-uploader btn-outline-dark downloadChecksum"
        href="/upload/${id}?format=csv"
        download="checksums_${id}.csv"
    >
        <i class="btn-icon fa-fw fa-regular fa-file-code"></i>
        <span>Download Checksum Report</span>
    </a>
</#macro>

<#macro data title files>
    <script id="${title}-data" type="application/json">
        ${files?no_esc}
    </script>
</#macro>
