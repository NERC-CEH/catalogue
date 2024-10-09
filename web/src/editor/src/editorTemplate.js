import _ from 'underscore'

export default _.template(`
<div class="editor">
  <div class="editor-menu">
    <div>
      <div class="editor-nav"> </div>
      <div class="editor-buttons">
        <button id="editorBack" class="btn btn-sm btn-light border" disabled><i class="fa-solid fa-chevron-left"></i> Back</button>
        <button id="editorNext" class="btn btn-sm btn-light border">Next <i class="fa-solid fa-chevron-right"></i></button>
        <button id="editorSave" class="btn btn-sm btn-light border">Save <i class="fa-regular fa-save"></i></button>
        <button id="editorExit" class="btn btn-sm btn-light border">Exit <i class="fa-solid fa-power-off"></i></button>
        <button id="editorDelete" class="btn btn-sm btn-danger">Delete <i class="fa-solid fa-times"></i></button>
      </div>
      </div>
    </div>

  <div id="editor" class="editor-body container-fluid" role="form"></div>

</div>
`)
