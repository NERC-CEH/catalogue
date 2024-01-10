import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-1">
    <label class="control-label" for="descriptiveKeyword<%= data.index %>type">Type</label>
  </div>
  <div class="col-sm-11">
    <select data-name="type" class="editor-input type" id="descriptiveKeyword<%= data.index %>type">
      <option value="" selected >- Select Type (optional) -</option>
      <optgroup label="Keyword type" class="optgroup optgroup-eidc">
        <option value="Catalogue topic">Catalogue topic</option>
        <option value="dataCentre">Data Centre</option>
        <option value="discipline">Discipline</option>
        <option value="instrument">Instrument</option>
        <option value="place">Place</option>
        <option value="project">Project</option>
        <option value="stratum">Stratum</option>
        <option value="taxon">Taxon</option>
        <option value="temporal">Temporal</option>
        <option value="theme">Theme</option>
      </optgroup>
    </select>
  </div>
</div>
<div class="keywords row col-sm-11 col-sm-offset-1"></div>
<div class="row">  
  <div class="keyword-add">
    <button class="editor-button add">Add <span class="fa-solid fa-plus" aria-hidden="true"></span></button>
  </div>
</div>
`)
