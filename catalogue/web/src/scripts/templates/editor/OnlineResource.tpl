<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="onlineResource<%= data.index %>Name">Name</label>
  </div>
  <div class="col-sm-6 col-lg-6">
    <input data-name="name" id="onlineResource<%= data.index %>Name" class="editor-input" value="<%= data.name %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label for="onlineResource<%= data.index %>Function">Function</label>
  </div>
  <div class="col-sm-3 col-lg-3">
    <select data-name="function" id="onlineResource<%= data.index %>Function" class="editor-input">
      <option value="" selected >- Select Function -</option>
      <option value="download">Download</option>
      <option value="information">Information</option>
      <option value="offlineAccess">Offline Access</option>
      <option value="order">Order</option>
      <option value="search">Search</option>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="onlineResource<%= data.index %>Description">Description</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <textarea rows="3" data-name="description" id="onlineResource<%= data.index %>Description" class="editor-textarea"><%= data.description %></textarea>
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="onlineResource<%= data.index %>Url">URL</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="url" id="onlineResource<%= data.index %>Url" class="editor-input" value="<%= data.url %>">
  </div>
</div>