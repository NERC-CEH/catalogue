import _ from 'underscore'

export default _.template(`
<div class="editor">
  <div class="editor-menu">
    <div>
      <div class="editor-nav"> </div>
      <div class="editor-buttons">
        <button id="editorBack" accesskey="<" class="btn btn-sm btn-outline-secondary" disabled><i class="fa-solid fa-chevron-left"></i> Back</button>
        <button id="editorNext" accesskey=">" class="btn btn-sm btn-outline-secondary">Next <i class="fa-solid fa-chevron-right"></i></button>
        <button id="editorSave" accesskey="s" class="btn btn-sm btn-outline-secondary">Save <i class="fa-regular fa-save"></i></button>
        <button id="editorExit" accesskey="x" class="btn btn-sm btn-outline-secondary">Exit <i class="fa-solid fa-power-off"></i></button>
        <button id="editorDelete" class="btn btn-sm btn-danger">Delete <i class="fa-solid fa-times"></i></button>
        <span id="editorAjax" role="status" aria-live="polite" aria-label="Working…"><i class="fa-solid fa-spinner fa-spin"></i></span>
      </div>
      </div>
    </div>

  <div id="editor" class="editor-body container-fluid" role="form"></div>

</div>
`)
