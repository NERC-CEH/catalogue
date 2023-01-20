<div class="row">
  <div class="col-sm-1">
    <label>Search</label>
  </div>
  <div class="col-sm-11 catalogueSearch">
    <input data-name="title" value="<%= data.title %>"  id="relationship<%= data.index %>Name" class="form-control autocomplete" placeholder="Search the catalogue...">
  </div>
</div>
<div class="row">
   <div class="col-sm-2 col-sm-offset-1">
    <label>Relationship</label>
   </div>
   <div class="col-sm-9">
    <select data-name="rel" id="relationship<%= data.index %>Rel" class="editor-input rel">
      <optgroup class="optiongroup">
        <option value='http://purl.org/dc/terms/references'>RELATED TO</option>
      </optgroup>
      <optgroup label="EIDC relationships" class="optiongroup optiongroup-eidc">
        <option value='https://vocabs.ceh.ac.uk/eidc#generates'>GENERATES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. code or model generates a dataset)</option>
        <option value='https://vocabs.ceh.ac.uk/eidc#memberOf'>MEMBER OF&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a dataset is a member of a data collection)</option>
        <option value='https://vocabs.ceh.ac.uk/eidc#supersedes'>SUPERSEDES</option>
        <option value='https://vocabs.ceh.ac.uk/eidc#uses'>USES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a service uses a dataset)</option>
      </optgroup>
      <optgroup label="eLTER relationships" class="optiongroup optiongroup-elter">
        <option value='https://onto.lter-europe.org#generates'>GENERATES&nbsp;&nbsp;&nbsp;&nbsp;(e.g. code or model generates a dataset)</option>
        <option value='https://onto.lter-europe.org#inputTo'>INPUT TO&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. dataset is input to code or model)</option>
        <option value='https://onto.lter-europe.org#uses'>USES&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. a service uses a dataset)</option>
      </optgroup>
      <optgroup label="PROV-O relationships" class="optiongroup optiongroup-prov optiongroup-elter">
        <option value='http://www.w3.org/ns/prov#wasGeneratedBy'>prov:wasGeneratedBy </option>
        <option value='http://www.w3.org/ns/prov#uses'>prov:uses </option>
      </optgroup>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-sm-offset-1">
    <label>Name</label>
  </div>
  <div class="col-sm-9">
    <input data-name="title" id="relationship<%= data.index %>Title" class="editor-input title" value="<%= data.title %>" autocomplete="off" disabled="true">
  </div>
</div>

<div class="hidden">
  <input data-name="identifier" id="relationship<%= data.index %>Identifier" class="editor-input identifier" value="<%= data.identifier %>">
  <input data-name="associationType" id="relationship<%= data.index %>AssociationType" class="editor-input associationType " value="<%= data.associationType %>">
</div>