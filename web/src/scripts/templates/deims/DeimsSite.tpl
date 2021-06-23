
<div class="row">
  <div class="col-sm-1">
    <label>Search</label>
  </div>
  <div class="col-sm-11 catalogueSearch">
    <input data-name="title" value="<%= data.title %>"  id="deims<%= data.index %>Name" class="form-control autocomplete" placeholder="Search the catalogue...">
  </div>
</div>
<div class="row">
   <div class="col-sm-2 col-sm-offset-1">
    <label>Name</label>
   </div>
   <div class="col-sm-9">
    <input data-name="title" id="deims<%= data.index %>Title" class="editor-input title" value="<%= data.title %>" autocomplete="off" disabled="true">
  </div>
 </div>

 <div class="row">
    <div class="col-sm-2 col-sm-offset-1">
     <label>Url</label>
    </div>
    <div class="col-sm-9">
     <input data-name="url" id="deims<%= data.index %>Url" class="editor-input url" value="<%= data.url %>" autocomplete="off" disabled="true">
   </div>
  </div>

<div class="hidden">
  <input data-name="id" id="deims<%= data.index %>id" class="editor-input id" value="<%= data.id %>">
</div>
