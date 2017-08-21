<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Upload Files">
    <div id='documents-upload' class='documents-upload'>
        <div class="container">
            <h1>Documents</h1>
            <a class="btn btn-default navbar-btn" href="/documents/${guid}">Return to metadata</a>
        </div>
        <div id='checksums' class="container checksums">
            <table class="table table-striped table-hover">
                <thead>
                    <tr>
                        <th>Checksum</th>
                        <th>File</th>
                        <#if permission.userCanUpload(guid)>
                        <th id='delete'></th>
                        </#if>
                    </tr>
                </thead>
                <tbody class='checksums-list'>
                <#list checksums as checksum>
                    <tr data-file="${checksum.filename}">
                        <td class='checksum-value'>${checksum.getMD5Hash()}</td>
                        <td class='checksum-file'>${checksum.filename}</td>
                        <#if permission.userCanUpload(guid)>
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
            <#if permission.userCanUpload(guid)>
                <div class="panel panel-default">
                    <div class="panel-heading">Finalize</div>
                    <div class="panel-body">
                        <p>Once you have uploaded all select finish for them to be finalized</p>
                        <b>You cannot undo this action, make sure you have all appropiate files uploaded</b>
                        <p class='finish-message text-danger'></p>
                        <div class="btn-group btn-group-justified" role="group" aria-label="Justified button group">
                            <a class="btn btn-success finish" role="button" disabled>
                                <i class="glyphicon glyphicon-ban-circle"></i> Finish
                            </a>
                        </div>
                    </div>
                </div>
            </#if>
        </div>
        <#if permission.userCanUpload(guid)>
            <div id="dropzone" class="container">
                <h2>Upload Files</h2>
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
            </div>
        </#if>
    </div>
</@skeleton.master>