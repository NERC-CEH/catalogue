<div class="vocabularies"/>
<h5>Search for keywords in selected vocabularies</h5>
<div class="row">
  <div class="col-sm-1">
    <label>Search</label>
  </div>
  <div class="col-sm-11 catalogueSearch">
    <input data-name="label" value="<%= data.label %>"  id="keywords<%= data.index %>Name" class="form-control autocomplete" placeholder="Search for Keywords...">
  </div>
</div>
<div class="row">
   <div class="col-sm-2 col-sm-offset-1">
    <label>Name</label>
   </div>
   <div class="col-sm-9">
    <input data-name="label" id="keywords<%= data.index %>Label" class="editor-input label" value="<%= data.label %>" autocomplete="off" disabled="true">
  </div>
</div>
<div class="row">
    <div class="col-sm-2 col-sm-offset-1">
        <label>Url</label>
    </div>
    <div class="col-sm-9">
        <input data-name="url" id="keywords<%= data.index %>Url" class="editor-input url" value="<%= data.url %>" autocomplete="off">
    </div>
</div>