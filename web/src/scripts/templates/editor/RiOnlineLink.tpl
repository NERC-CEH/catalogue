<div class="row">
  <div class="col-sm-2">
    <label for="<%= data.modelAttribute %><%= data.index %>Name">Name/description</label>
  </div>
  <div class="col-sm-10">
    <input data-name="name" id="<%= data.modelAttribute %><%= data.index %>Name" class="editor-input" value="<%= data.name %>" autocomplete="off">
  </div>
</div>
<div class="row">  
  <div class="col-sm-2">
    <label for="<%= data.modelAttribute %><%= data.index %>URL">Address</label>
  </div>
  <div class="col-sm-10">
    <input data-name="url" id="<%= data.modelAttribute %><%= data.index %>URL" class="editor-input" value="<%= data.url %>"  autocomplete="off">
  </div>
</div>