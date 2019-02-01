<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="supplemental<%= data.index %>Type">Type</label>
  </div>
  <div class="col-sm-10">
    <select data-name="type" class="editor-input type" id="supplemental<%= data.index %>Type">
      <option value="dataPaper">DATA PAPER (that describes this resource)</option>
      <option value="isReferencedBy">REFERENCED IN... (an article that references or cites this resource)</option>
      <option value="relatedArticle">RELATED ARTICLE (or grey literature) which is relevant but which DOESN'T cite this resource</option>
      <option value="relatedDataset">RELATED DATASET (but which is NOT hosted by EIDC)</option>
      <option value="website">WEBSITE (e.g. project website)</option>
      <option value="" selected>Other</option>
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
    <textarea data-name='description' class="editor-textarea" id="supplemental<%= data.index %>Description" rows="7"><%= data.description %></textarea>
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
