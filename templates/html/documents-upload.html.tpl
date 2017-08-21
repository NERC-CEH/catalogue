<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Upload Files">
    <div id='documents-upload'>
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
                        <td>${checksum.getMD5Hash()}</td>
                        <td>${checksum.filename}</td>
                        <#if permission.userCanUpload(guid)>
                        <td class="text-center">
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
            <button type="button" class="btn btn-block btn-success" data-toggle="modal" data-target="#finishConfirmation">
                <i class="glyphicon glyphicon-ok"></i> Finish
            </button>
            <div class="modal fade" id="finishConfirmation" tabindex="-1" role="dialog" aria-labelledby="finishConfirmationLabel">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="finishConfirmationLabel">Finish Confirmation</h4>
                    </div>
                    <div class="modal-body">
                        <p>
                            You are about to move these files to <b>finished</b>
                            <br />
                            Once you do this you will not be able to undo it
                            <br />
                            <br />
                            Are you sure you want to upload these files?
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                        <button id='finish' type="button" class="btn btn-success">Finish</button>
                    </div>
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