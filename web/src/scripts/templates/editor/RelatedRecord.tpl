
<div class="row">
  <div class="col-sm-1">
    <label>Search</label>
  </div>
  <div class="col-sm-11 catalogueSearch">
    <input data-name="title" value="<%= data.title %>"  id="relationship<%= data.index %>Name" class="form-control autocomplete" placeholder="Search the catalogue...">
  </div>
</div>
 <div class="row">
   <div class="col-sm-1 col-sm-offset-1">
    <label>Relationship</label>
   </div>
   <div class="col-sm-10">
    <select  data-name="rel" id="relationship<%= data.index %>Rel" class="editor-input rel">
        <option value='http://vocabs.ceh.ac.uk/eidc#produces'>PRODUCES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a model produces a dataset)</option>
        <option value='http://vocabs.ceh.ac.uk/eidc#relatedTo'>RELATED TO</option>
        <option value='http://vocabs.ceh.ac.uk/eidc#supersedes'>SUPERSEDES</option>
        <option value='http://vocabs.ceh.ac.uk/eidc#uses'>USES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a service uses a dataset)</option>
        <option value='http://vocabs.ceh.ac.uk/eidc#partOf'>PART OF&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a dataset is part of a data collection)</option>
      </select>
  </div>
</div>
<div class="row">
   <div class="col-sm-1 col-sm-offset-1">
    <label>Name</label>
   </div>
   <div class="col-sm-10">
    <input data-name="title" id="relationship<%= data.index %>Title" class="editor-input title" value="<%= data.title %>" autocomplete="off" disabled="true">
  </div>
 </div>

<div class="hidden">
  <input data-name="identifier" id="relationship<%= data.index %>Identifier" class="editor-input identifier" value="<%= data.identifier %>">
  <input data-name="associationType" id="relationship<%= data.index %>AssociationType" class="editor-input associationType " value="<%= data.associationType %>">
</div>
