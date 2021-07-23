<div class="vocabularies"/>
<button class='select-all'>Search All Catalogues</button>
<button class='search-selected'>Search Selected</button>
<div class="row">
  <div class="col-sm-1">
    <label>Keywords</label>
  </div>
  <div class="col-sm-11 keywordSearch">
    <input data-name="title" value="<%= data.title %>"  id="keyword<%= data.index %>Name" class="form-control autocomplete" placeholder="Search for Keywords...">
  </div>
</div>
<div class="row">
   <div class="col-sm-2 col-sm-offset-1">
    <label>Name</label>
   </div>
   <div class="col-sm-9">
    <input data-name="title" id="keyword<%= data.index %>Title" class="editor-input title" value="<%= data.label %>" autocomplete="off" disabled="true">
  </div>
</div>
<div class="row">
    <div class="col-sm-2 col-sm-offset-1">
        <label>Url</label>
    </div>
    <div class="col-sm-9">
        <input data-name="url" id="keyword<%= data.index %>Url" class="editor-input url" value="<%= data.url %>" autocomplete="off" disabled="true">
    </div>
</div>
<div class="hidden">
  <input data-name="vocabId" id="keyword<%= data.index %>vocabId" class="editor-input vocabId" value="<%= data.vocabId %>">
</div>