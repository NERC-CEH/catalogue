</div>
<div class="row">  
  <div class="col-sm-2">
    <label class="control-label" for="supplemental<%= data.index %>Description">Citation</label>
  </div>
  <div class="col-sm-10">
    <textarea data-name='description' class="editor-textarea" id="supplemental<%= data.index %>Description" rows="4"><%= data.description %></textarea>
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="supplemental<%= data.index %>URL">URL/DOI</label>
  </div>
  <div class="col-sm-10">
    <input data-name='url' class="editor-input" id="supplemental<%= data.index %>URL" value="<%= data.url %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="supplemental<%= data.index %>NoraID">NORA ID</label>
  </div>
  <div class="col-sm-10">
    <input data-name='noraID' class="editor-input" id="supplemental<%= data.index %>NoraID" value="<%= data.noraID %>">
  </div>
</div>