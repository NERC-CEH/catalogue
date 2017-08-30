<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="supplemental<%= data.index %>Type">Type of information</label>
  </div>
  <div class="col-sm-10">
    <select data-name="type" class="editor-input type" id="supplemental<%= data.index %>Type">
      <option value="" selected>General</option>
      <option value="isCitedBy">Article that cites/references this resource</option>
      <option value="">Article that DOESN'T cite/reference this resource</option>
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
    <label class="control-label" for="supplemental<%= data.index %>URL">URL</label>
  </div>
  <div class="col-sm-10">
    <input data-name='url' class="editor-input" id="supplemental<%= data.index %>URL" value="<%= data.url %>">
  </div>
</div>
