import _ from 'underscore'

export default _.template(`
  <div class="row">
      <div class="col-lg-2">
          <label class="control-label">Fileset Name</label>
      </div>
      <div class="col-lg-4">
          <input data-name='filesetName' class="editor-input" value="<%= data.filesetName %>">
      </div>
      <div class="col-lg-2">
          <label class="control-label">Encoding Format</label>
      </div>
      <div class="col-lg-4">
          <input data-name='encodingFormat' class="editor-input" value="<%= data.encodingFormat %>">
      </div>
  </div>
  <div class="row">
      <div class="col-lg-2">
          <label class="control-label">Includes</label>
      </div>
      <div class="col-lg-4">
          <input data-name='includes' class="editor-input" value="<%= data.includes %>">
      </div>
      <div class="col-lg-6">
        <button class="editor-button dropdown-toggle" data-bs-toggle="dropdown" type="button" id="addObservedPropertyBtn" aria-expanded="false">
            Add Observed Property
        </button>
        <ul class="dropdown-menu" aria-labelledby="addObservedPropertyBtn"></ul>
      </div>
  </div>

  <div class="styling-box observedProperty mt-4"></div>
`)
