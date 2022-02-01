<ol id="editorNav" class="breadcrumb"></ol>

<div id="editor" class="container-fluid" role="form"></div>
<div class="navbar navbar-default navbar-fixed-bottom">
  <div class="container-fluid">
    <div class="navbar-left">
      <button id="editorDelete" class="btn btn-sm btn-danger navbar-btn">Delete <i class="fas fa-times"></i></button>
    </div>
    <div class="navbar-right">
      <span id="editorAjax" class="navbar-text">Saving: <img src="/static/img/ajax-loader.gif"></span>
      <div class="btn-group">
        <button id="editorBack" class="btn btn-sm btn-default navbar-btn" disabled><i class="fas fa-chevron-left"></i> Back</button>
        <button id="editorNext" class="btn btn-sm btn-default navbar-btn">Next <i class="fas fa-chevron-right"></i></button>
      </div>
        <button id="editorSave" class="btn btn-sm btn-default navbar-btn">Save <i class="far fa-save"></i></button>
      <button id="editorExit" class="btn btn-sm btn-default navbar-btn">Exit <i class="fas fa-power-off"></i></button>
    </div>
  </div>
</div>
<div id="confirmExit" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h4 class="modal-title">Are you sure?</h4>
      </div>
      <div class="modal-body">
        <p>There are unsaved changes to this record<br>Do you want to exit without saving?</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-primary" data-dismiss="modal">No</button>
        <button type="button" class="btn btn-default" id="exitWithoutSaving">Yes, exit without saving</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<div id="confirmDelete" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-body">
        <h2 class="text-red"><i class="fas fa-exclamation-triangle"></i> <strong>Are you sure?</strong></h2>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-primary" data-dismiss="modal">No</button>
        <button type="button" class="btn btn-default" id="confirmDeleteYes">Yes, delete this record</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<div id="editorErrorMessage" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h4 class="modal-title">There was a problem communicating with the server</h4>
      </div>
      <div class="modal-body">
        <p>Server response:</p>
        <pre id="editorErrorMessageResponse"></pre>
        <p>Please save this record locally by copying the text below to a file.</p>
        <pre id="editorErrorMessageJson" class="pre-scrollable"></pre>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">OK</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<div id="editorValidationMessage" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h4 class="modal-title">Validation Errors</h4>
      </div>
      <div class="modal-body"></div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">OK</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->