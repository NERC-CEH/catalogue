<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Upload Files">
    <div id='documents-upload' class="container documents-upload">
        <section class='section'>
            <h1 class='title'>
                <small class="title-type">${type}</small>
                <span>${title}</span>
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
        <#if canUpload>
            <section class='section'>
                <h3 class='subtitle'>Finish</h3>
                <p>
                    Once you have finished uploading your documents click below in order to progress to the next stage
                    <br />
                    <b>This action can not be undone</b>
                    <p class='finish-message text-danger'></p>
                    <div class="btn-group btn-group-justified" role="group" aria-label="Justified button group">
                        <a class="btn btn-success finish" role="button" disabled>
                            <i class="glyphicon glyphicon-ban-circle"></i> Finish
                        </a>
                    </div>
                </p>
            </section>
        </#if>
        <section class='section'>
            <h3 class='subtitle'>Documents</h3>
            <table class="table table-striped table-hover">
                <thead>
                    <tr>
                        <th>Checksum</th>
                        <th>File</th>
                        <#if canUpload>
                            <th id='delete'></th>
                        </#if>
                    </tr>
                </thead>
                <tbody class='checksums-list'>
                <#list checksums as checksum>
                    <tr data-file="${checksum.filename}">
                        <td class='checksum-value'>${checksum.getMD5Hash()}</td>
                        <td class='checksum-file'>${checksum.filename}</td>
                        <#if canUpload>
                            <td class="checksum-delete text-center">
                                <button class="btn btn-block btn-danger delete" data-file="${checksum.filename}">
                                    <i class="glyphicon glyphicon-trash"></i> Delete
                                </button>
                            </td>
                        </#if>
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
                            <i class="glyphicon glyphicon-plus"></i>
                            <span>Add files...</span>
                        </a>
                        <a class="btn btn-success upload-all" role="button" disabled>
                            <i class="glyphicon glyphicon-open"></i>
                            <span>Upload All</span>
                        </a>
                        <a class="btn btn-success cancel-all" role="button" disabled>
                            <i class="glyphicon glyphicon-remove"></i>
                            <span>Cancel All</span>
                        </a>
                    </div>
                    <div class="dz dropzone-area">
                        <h1 class="title">
                            <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span> Loading Dropzone...
                        </h1>
                        <div id="previews"></div>
                    </div>
                </div>
            </section>
        </#if>
    </div>
</@skeleton.master>