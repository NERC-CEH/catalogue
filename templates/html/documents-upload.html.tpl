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
                            <i class="fa fa-ban"></i> Finish
                        </a>
                    </div>
                </p>
            </section>
        </#if>
        <section class='section'>
            <h3 class='subtitle'>Documents</h3>
            <p>These are the files which have been uploaded. The checksum is a value used to guarantee the contents of the file haven't changed. It is generated for you when you upload your files. You may be asked to provide this value in the future.</p>
            <table class="table table-striped table-hover">
                <thead>
                    <tr>
                        <th>File</th>
                        <th>Checksum</th>
                        <#if canUpload>
                            <th id='delete'></th>
                        </#if>
                        <#if canChangeType>
                            <th id='canChangeType'></th>
                        </#if>
                    </tr>
                </thead>
                <tbody class='checksums-list'>
                <#list documentUpload.data?values as file>
                    <tr data-file="${file.name}">
                        <td class='checksum-file'>${file.name}</td>
                        <td class='checksum-value'>${file.hash}</td>
                        <#if canUpload>
                            <td class="checksum-delete text-center">
                                <button class="btn btn-block btn-danger delete" data-file="${file.name}">
                                    <i class="fa fa-trash-o"></i> Delete
                                </button>
                            </td>
                        </#if>
                        <#if canChangeType>
                            <td>
                                <form action="">
                                    <div class="change-type">
                                        <input type="radio" id="to-data-${file.name}" name="type" value="data" checked />
                                        <label for="to-data-${file.name}">Data</label>
                                    </div>
                                    <div class="change-type">
                                        <input type="radio" id="to-meta-${file.name}" name="type" value="meta" />
                                        <label for="to-meta-${file.name}">Meta</label>
                                    </div>
                                </form>
                            </td>
                        </#if>
                    </tr>
                </#list>
                <#list documentUpload.meta?values as file>
                    <tr data-file="${file.name}">
                        <td class='checksum-file'>${file.name}</td>
                        <td class='checksum-value'>${file.hash}</td>
                        <#if canUpload>
                            <td class="checksum-delete text-center">
                                <button class="btn btn-block btn-danger delete" data-file="${file.name}">
                                    <i class="fa fa-trash-o"></i> Delete
                                </button>
                            </td>
                        </#if>
                        <#if canChangeType>
                            <td>
                                <form action="">
                                    <div class="change-type">
                                        <input type="radio" id="to-data-${file.name}" name="type" value="data" />
                                        <label for="to-data-${file.name}">Data</label>
                                    </div>
                                    <div class="change-type">
                                        <input type="radio" id="to-meta-${file.name}" name="type" value="meta" checked />
                                        <label for="to-meta-${file.name}">Meta</label>
                                    </div>
                                </form>
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
    </div>
</@skeleton.master>