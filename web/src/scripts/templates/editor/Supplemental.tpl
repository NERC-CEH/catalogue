<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="supplemental<%= data.index %>Type">Type</label>
  </div>
  <div class="col-sm-10">
    <select data-name="type" class="editor-input type" id="supplemental<%= data.index %>Type">
      <option value="" selected>General</option>
      <option value="isCitedBy">Article that cites this resource</option>
      <option value="relatedArticle">Article that is related but which DOESN'T cite this resource</option>
      <option value="website">Website</option>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="supplemental<%= data.index %>Name">Name</label>
  </div>
  <div class="col-sm-10">
    <input data-name='name' class="editor-input" id="supplemental<%= data.index %>Name" value="<%= data.name %>">
  </div>
</div>
<div class="row">  
  <div class="col-sm-2">
    <label class="control-label" for="supplemental<%= data.index %>Description">Description</label>
  </div>
  <div class="col-sm-10">
    <textarea data-name='description' class="editor-textarea" id="supplemental<%= data.index %>Description" value="<%= data.description %>" rows="7"></textarea>
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
