<div class="row">
  <div class="col-sm-1">
    <label>Search</label>
  </div>
  <div class="col-sm-5 catalogueSearch">
    <input data-name="titleFrom" value="<%= data.title %>"  id="relationship<%= data.index %>NameFrom" class="form-control autocomplete autocompleteFrom" placeholder="Search the catalogue...">
  </div>
  <div class="col-sm-1">
    <label>Name</label>
  </div>
  <div class="col-sm-5">
    <input data-name="titleFrom" id="relationship<%= data.index %>TitleFrom" class="editor-input titleFrom" value="<%= data.titleFrom %>" autocomplete="off" disabled="true">
  </div>
</div>

<div class="row">
  <div class="col-sm-2 col-sm-offset-1">
    <label>Relationship</label>
  </div>
   <div class="col-sm-9">
    <select data-name="rel" id="relationship<%= data.index %>Rel" class="editor-input rel">
      <optgroup class="optiongroup">
        <option value='https://onto.lter-europe.org#relatedTo'>RELATED TO</option>
      </optgroup>
      <optgroup label="EIDC relationships" class="optiongroup-eidc">
        <option value='https://vocabs.ceh.ac.uk/eidc#generates'>GENERATES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. code or model generates a dataset)</option>
        <option value='https://vocabs.ceh.ac.uk/eidc#memberOf'>MEMBER OF&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a dataset is a member of a data collection)</option>
        <option value='https://vocabs.ceh.ac.uk/eidc#supersedes'>SUPERSEDES</option>
        <option value='https://vocabs.ceh.ac.uk/eidc#uses'>USES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a service uses a dataset)</option>
      </optgroup>
      <optgroup label="eLTER relationships" class="optiongroup-elter">
        <option value='https://onto.lter-europe.org#generates'>GENERATES&nbsp;&nbsp;&nbsp;&nbsp;(e.g. code or model generates a dataset)</option>
        <option value='https://onto.lter-europe.org#inputTo'>INPUT TO&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. dataset is input to code or model)</option>
        <option value='https://onto.lter-europe.org#uses'>USES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a service uses a dataset)</option>
      </optgroup>
    </select>
  </div>
</div>

<div class="row">
  <div class="col-sm-1">
    <label>Search</label>
  </div>
  <div class="col-sm-5 catalogueSearch">
    <input data-name="titleTo" value="<%= data.title %>"  id="relationship<%= data.index %>NameTo" class="form-control autocomplete autocompleteTo" placeholder="Search the catalogue...">
  </div>
  <div class="col-sm-1">
    <label>Name</label>
  </div>
  <div class="col-sm-5">
    <input data-name="titleTo" id="relationship<%= data.index %>TitleTo" class="editor-input titleTo" value="<%= data.titleTo %>" autocomplete="off" disabled="true">
  </div>
</div>
<div class="row">
  <div class="col-sm-1">
    <label>Description</label>
  </div>
  <div class="col-sm-11">
    <input data-name="description" id="relationship<%= data.index %>Description" class="editor-input description" value="<%= data.description %>" autocomplete="off">
  </div>
</div>

<div class="hidden">
  <input data-name="identifierFrom" id="relationship<%= data.index %>IdentifierFrom" class="editor-input identifierFrom" value="<%= data.identifierFrom %>">
  <input data-name="identifierTo" id="relationship<%= data.index %>IdentifierTo" class="editor-input identifierTo" value="<%= data.identifierTo %>">
  <input data-name="associationType" id="relationship<%= data.index %>AssociationType" class="editor-input associationType " value="<%= data.associationType %>">
</div>
