import _ from 'underscore'

export default _.template(`
  <div class="row">
      <div class="col-xxl-2">
          <label class="control-label">Fileset name</label>
      </div>
      <div class="col-xxl-4">
          <input data-name='filesetName' placeholder="e.g., csv files" class="editor-input" value="<%= data.filesetName %>">
      </div>
      <div class="col-xxl-2">
          <label class="control-label">Encoding format</label>
      </div>
      <div class="col-xxl-4">
          <input list="encodingFormatList" data-name='encodingFormat' class="editor-input" placeholder="e.g., text/csv" value="<%= data.encodingFormat %>">
      </div>
  </div>
  <div class="row">
      <div class="col-xxl-2">
          <label class="control-label">Files included</label>
      </div>
      <div class="col-xxl-4">
          <input data-name='includes' placeholder="Use wildcards. E.g., *.csv or data_20??.cf" class="editor-input" value="<%= data.includes %>">
      </div>
      <div class="col-xxl-6 d-flex">
        <% if (data.fetchVariablesButton) { %>
          <button class="legilo-variables-btn editor-button px-4 me-2"><i class="fa-solid fa-wand-magic-sparkles"></i> Suggest</button>
        <% } %>
        <button class="editor-button dropdown-toggle" data-bs-toggle="dropdown" type="button" id="addObservedPropertyBtn" aria-expanded="false">
            Add property
        </button>
        <ul class="dropdown-menu" aria-labelledby="addObservedPropertyBtn"></ul>
      </div>
  </div>
  <datalist id="encodingFormatList">
    <option value="text/csv">CSVs</option>
    <option value="image/tiff">Geotiffs</option>
    <option value="application/netcdf">NetCDFs</option>
  </datalist>

  <div class="legilo-variables-view mt-4"></div>
  <div class="styling-box observedProperty"></div>
`)
