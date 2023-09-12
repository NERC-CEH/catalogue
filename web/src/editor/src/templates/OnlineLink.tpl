<div class="row">  
  <div class="col-sm-2">
    <label for="<%= data.modelAttribute %><%= data.index %>URL">Address</label>
  </div>
  <div class="col-sm-10">
    <input data-name="url" id="<%= data.modelAttribute %><%= data.index %>URL" class="editor-input" value="<%= data.url %>"  autocomplete="off">
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label for="<%= data.modelAttribute %><%= data.index %>Function">Type</label>
  </div>
  <div class="col-sm-3">
  <input data-name="function" id="<%= data.modelAttribute %><%= data.index %>Function" class="editor-input" value="<%= data.function %>" list="<%= data.modelAttribute %>List" autocomplete="off">
  </div>
  <div class="col-sm-1">
    <label for="<%= data.modelAttribute %><%= data.index %>Name">Name</label>
  </div>
  <div class="col-sm-6">
    <input data-name="name" id="<%= data.modelAttribute %><%= data.index %>Name" class="editor-input" value="<%= data.name %>" autocomplete="off">
  </div>
</div>
<div class="row">  
  <div class="col-sm-2">
    <label for="<%= data.modelAttribute %><%= data.index %>Description">Description</label>
  </div>
  <div class="col-sm-10">
    <textarea data-name="description" id="<%= data.modelAttribute %><%= data.index %>Description" class="editor-textarea" rows="3"><%= data.description %></textarea>
  </div>
</div>
<datalist id="<%= data.modelAttribute %>List"><%= data.listAttribute%></datalist>
