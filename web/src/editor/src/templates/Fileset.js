import _ from 'underscore'

export default _.template(`
  <div class="row">
      <div class="col-lg-2">
          <label class="control-label">Fileset</label>
      </div>
      <div class="col-lg-4">
          <input data-name='filesetName' placeholder="e.g., csv files" class="editor-input" value="<%= data.filesetName %>">
      </div>
      <div class="col-lg-2">
          <label class="control-label">Encoding format</label>
      </div>
      <div class="col-lg-4">
          <input data-name='encodingFormat' class="editor-input" placeholder="e.g., text/csv" value="<%= data.encodingFormat %>">
      </div>
  </div>
  <div class="row">
      <div class="col-lg-2">
          <label class="control-label">Filename pattern</label>
      </div>
      <div class="col-lg-4">
          <input data-name='includes' placeholder="e.g., *.csv" class="editor-input" value="<%= data.includes %>">
      </div>
      <div class="col-lg-6">
        <button class="editor-button dropdown-toggle" data-bs-toggle="dropdown" type="button" id="addObservedPropertyBtn" aria-expanded="false">
            Add property
        </button>
        <ul class="dropdown-menu" aria-labelledby="addObservedPropertyBtn"></ul>
      </div>
  </div>

  <div class="styling-box observedProperty mt-4"></div>
`)
