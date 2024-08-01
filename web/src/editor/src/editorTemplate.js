import _ from 'underscore'

export default _.template(`
<ol id="editorNav" class="breadcrumb"></ol>

<div id="editor" class="container-fluid" role="form"></div>
<div class="editor-footer">
    <div class="container-fluid d-flex justify-content-between">
      <div class="left">
        <button id="editorDelete" class="btn btn-sm btn-danger">Delete <i class="fa-solid fa-times"></i></button>
      </div>
      <div class="right">
        <button id="editorBack" class="btn btn-sm btn-light" disabled><i class="fa-solid fa-chevron-left"></i> Back</button>
        <button id="editorNext" class="btn btn-sm btn-light">Next <i class="fa-solid fa-chevron-right"></i></button>
        <button id="editorSave" class="btn btn-sm btn-light">Save <i class="fa-regular fa-save"></i></button>
        <button id="editorExit" class="btn btn-sm btn-light">Exit <i class="fa-solid fa-power-off"></i></button>
      </div>
    </div>
</div>
`)
