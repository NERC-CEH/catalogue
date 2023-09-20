import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-2">
    <label for="reference<%= data.index %>Citation">Citation</label>
  </div>
  <div class="col-sm-10">
    <textarea data-name="citation" rows="5" id="reference<%= data.index %>Citation" class="editor-textarea"><%= data.citation %></textarea>
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label for="reference<%= data.index %>Doi">DOI</label>
  </div>
  <div class="col-sm-10 col-md-4">
    <input data-name="doi" id="reference<%= data.index %>Doi" class="editor-input" value="<%= data.doi %>" placeholder="e.g. https://doi.org/10.5072/unique-id-1234">
  </div>
  <div class="col-sm-2">
    <label for="reference<%= data.index %>Nora">NORA url</label>
  </div>
  <div class="col-sm-10 col-md-4">
    <input data-name="nora" id="reference<%= data.index %>Nora" class="editor-input" value="<%= data.nora %>" placeholder="e.g. http://nora.nerc.ac.uk/d/eprint/xxxxxx">
  </div>
</div>
`)
