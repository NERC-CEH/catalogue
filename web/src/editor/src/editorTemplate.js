import _ from 'underscore'

export default _.template(`
<ol id="editorNav" class="breadcrumb"></ol>

<div id="editor" class="container-fluid" role="form"></div>
<div class="editor-footer">
    <div class="container-fluid">
      <div class="left">
        <button id="editorDelete" class="btn btn-sm btn-danger navbar-btn">Delete <i class="fa-solid fa-times"></i></button>
      </div>
      <div class="right">
        <button id="editorBack" class="btn btn-sm btn-default navbar-btn" disabled><i class="fa-solid fa-chevron-left"></i> Back</button>
        <button id="editorNext" class="btn btn-sm btn-default navbar-btn">Next <i class="fa-solid fa-chevron-right"></i></button>
        <button id="editorSave" class="btn btn-sm btn-default navbar-btn">Save <i class="fa-regular fa-save"></i></button>
        <button id="editorExit" class="btn btn-sm btn-default navbar-btn">Exit <i class="fa-solid fa-power-off"></i></button>
      </div>
    </div>
</div>
`)
