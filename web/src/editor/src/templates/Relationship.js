import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="relationship<%= data.index %>Relation">Relation</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input list="relationship<%= data.index %>relationships" data-name="relation" id="relationship<%= data.index %>Relation" class="editor-input" value="<%= data.relation %>">
    <datalist id="relationship<%= data.index %>relationships"></datalist>
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="relationship<%= data.index %>Target">Target</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="target" id="relationship<%= data.index %>Target" class="editor-input" value="<%= data.target %>">
  </div>
</div>
`)
