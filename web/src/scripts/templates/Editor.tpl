<ol id="editorNav" class="breadcrumb hidden-xs"></ol>
<div id="editor" class="container-fluid" role="form"></div>
<div class="navbar navbar-default navbar-fixed-bottom">
  <div class="container-fluid">
    <div class="navbar-left">
      <button id="editorDelete" class="btn btn-default navbar-btn">Delete <i class="glyphicon glyphicon-remove"></i></button>
    </div>
    <div class="navbar-right">
      <p id="editorAjax" class="navbar-text">Saving: <img src="/static/img/ajax-loader.gif"></p>
      <button id="editorBack" class="btn btn-default navbar-btn" disabled><i class="glyphicon glyphicon-chevron-left"></i> Back</button>
      <button id="editorNext" class="btn btn-default navbar-btn">Next <i class="glyphicon glyphicon-chevron-right"></i></button>
      <button id="editorSave" class="btn btn-default navbar-btn">Save <i class="glyphicon glyphicon-save"></i></button>
      <button id="editorExit" class="btn btn-default navbar-btn">Exit <i class="glyphicon glyphicon-off"></i></button>
    </div>
  </div>
</div>
<div id="confirmExit" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h4 class="modal-title">Confirm exit without saving</h4>
      </div>
      <div class="modal-body">
        <p>There are unsaved changes to this record.</p>
        <p>Do you want to exit without saving these changes?</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">No</button>
        <button type="button" class="btn btn-primary" id="exitWithoutSaving">Yes, exit without saving</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<div id="confirmDelete" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h4 class="modal-title">Confirm record deletion</h4>
      </div>
      <div class="modal-body">
        <p>Delete this record?</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">No</button>
        <button type="button" class="btn btn-primary" id="confirmDeleteYes">Yes, delete this record</button>
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
