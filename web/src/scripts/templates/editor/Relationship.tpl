<div class="col-sm-2 col-lg-2">
  <label for="relationship<%= data.index %>Relation">Relation</label>
</div>
<div class="col-sm-4 col-lg-4">
  <select data-name="relation" id="relationship<%= data.index %>Relation" class="editor-input">
  	<option value="http://purl.org/dc/terms/references">References</option>
  </select>
</div>
<div class="col-sm-1 col-lg-1">
  <label for="relationship<%= data.index %>Target">Target</label>
</div>
<div class="col-sm-5 col-lg-5">
  <input data-name="target" id="relationship<%= data.index %>Target" class="editor-input" value="<%= data.target %>">
</div>