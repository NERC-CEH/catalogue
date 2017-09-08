<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Upload Files">
    <div id='documents-upload' class="container documents-upload">
        <section class='section'>
            <h1 class='title'>
                <small class="title-type">${documentUpload.type}</small>
                <span>${documentUpload.title}</span>
            </h1>
        </section>
        <section class='section'>
            <h3 class='subtitle'>Status</h3>
            <p>${status}</p>
            <#if isScheduled && !userCanUpload>
                <p class='text-danger'>
                    This document is currently being worked on but you do not have permission to update
                    <br />
                    Please contact an admin for further details
                </p>
            </#if>
        </section>
        <section class='section ${(documentUpload.getFiles()?size == 0)?string("documents-is-inactive", "is-active")}'>
            <h3 class='subtitle'>Documents</h3>
            <p>These are the files which have been uploaded. The checksum is a value used to guarantee the contents of the file haven't changed. It is generated for you when you upload your files. You may be asked to provide this value in the future.</p>
            <table class="table table-hover" >
                <thead>
                    <tr>
                        <th>File</th>
                        <th>Checksum</th>
                        <#if canUpload>
                            <th id='delete'></th>
                            <th id='canChangeType'></th>
                        </#if>
                    </tr>
                </thead>
                <tbody class='checksums-list'>
                <#list documentUpload.getFiles() as file>
                    <tr>
                        <td class='checksum-file'>${file.name}</td>
                        <td class='checksum-value'>${file.hash}</td>
                        <#if canUpload>
                            <td class="checksum-delete text-center">
                                <button class="btn btn-block btn-danger delete is-initialising" disabled>
                                    <i class="fa fa-trash-o"></i> Delete
                                </button>
                            </td>
                        </#if>
                        <#if canUpload>
                            <td>
                               <div class="btn-group data-meta-toggle" data-toggle="buttons">
                                    <#if file.type == "DATA">
                                        <button class='btn btn-success to-meta is-initialising' disabled>DATA</button>
                                        <button class="btn to-data is-initialising" disabled>META</button>
                                    <#else>
                                        <button class='btn to-meta is-initialising' disabled>DATA</button>
                                        <button class="btn btn-success to-data is-initialising" disabled>META</button>
                                    </#if>
                                </div>
                            </td>
                        </#if>
                    </tr>
                </#list>
                </tbody>
            </table>
        </section>
        <section class='section ${(documentUpload.invalid?values?size == 0)?string("invalid-is-inactive", "is-active")}'>
            <h3 class='subtitle'>Invalid Files</h3>
            <p>These files need resolving, either their checksums have changed or there is an inconsitency with the information about files. This could be due to manually uploading large files or someone changing the auto generated data.</p>
            <table class="table table-hover" >
                <thead>
                    <tr>
                        <th>File</th>
                        <th>Checksum</th>
                        <th>Error</th>
                        <#if canUpload>
                            <th id='fix'></th>
                            <th id='deleteInvalid'></th>
                        </#if>
                    </tr>
                </thead>
                <tbody class="invalid-checksums-list">
                <#list documentUpload.getInvalid()?values as file>
                    <tr class='invalid-row'>
                        <td class='checksum-file'>${file.name}</td>
                        <td>${file.hash}</td>
                        <td>${file.getLatestComment()}</td>
                        <td class="text-center">
                            <#if file.type == "INVALID_HASH">
                                <button class="btn btn-block btn-success accept-invalid is-initialising" disabled>
                                    <i class="fa fa-check"></i> Update checksum
                                </button>
                            </#if>
                            <#if file.type == "UNKNOWN_FILE">
                                <button class="btn btn-block btn-success accept-invalid is-initialising" disabled>
                                    <i class="fa fa-check"></i> Add file
                                </button>
                            </#if>
                        </td>
                        <td class="text-center">
                            <button class="btn btn-block btn-danger is-initialising delete" disabled>
                                <i class="fa fa-trash-o"></i> Delete
                            </button>
                        </td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </section>
        <#if canUpload>
            <section class='section'>
                <h3 class='subtitle'>Upload</h3>
                <div class="dropzone">
                    <div class="btn-group btn-group-justified" role="group" aria-label="Justified button group">
                        <a class="btn btn-success fileinput-button" role="button" disabled>
                            <i class="fa fa-plus"></i>
                            <span>Add files...</span>
                        </a>
                        <a class="btn btn-success upload-all" role="button" disabled>
                            <i class="fa fa-upload"></i>
                            <span>Upload All</span>
                        </a>
                        <a class="btn btn-success cancel-all" role="button" disabled>
                            <i class="fa fa-times"></i>
                            <span>Cancel All</span>
                        </a>
                    </div>
                    <div class="dz dropzone-area">
                        <h1 class="title">
                            <span class="fa fa-refresh fa-spin"></span> Loading Dropzone...
                        </h1>
                        <div id="previews"></div>
                    </div>
                </div>
            </section>
        </#if>
        <#if canUpload && isScheduled>
            <section class='section ${(documentUpload.getFiles()?size == 0)?string("finish-is-inactive", "is-active")}'>
                <h3 class='subtitle'>Finish</h3>
                <p>
                    Once you have finished uploading your documents click below in order to progress to the next stage
                    <br />
                    <b>This action can not be undone</b>
                    <p class='finish-message text-danger'></p>
                    <div class="btn-group btn-group-justified" role="group" aria-label="Justified button group">
                        <a class="btn btn-success finish" role="button" disabled>
                            <i class="fa fa-ban"></i> Finish
                        </a>
                    </div>
                </p>
            </section>
        </#if>
    </div>
</@skeleton.master>