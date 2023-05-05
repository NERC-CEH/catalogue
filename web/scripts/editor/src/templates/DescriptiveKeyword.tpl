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
  <div class="col-sm-11 col-sm-offset-1">
    <div class="keyword-add hidden" id="catalogueTopic">
      <button class="editor-button dropdown-toggle" type="button" id="dropdownCehTopicMenu" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
        Choose Catalogue topics
        <span class="caret"></span>
      </button>
      <ul class="dropdown-menu predefined" aria-labelledby="dropdownCehTopicMenu">
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/1">Agriculture</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/20">Animal behaviour</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/2">Biodiversity</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/4">Climate and climate change</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/6">Ecosystem services</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/5">Environmental risk</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/7">Environmental survey</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/19">Evolutionary ecology</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/9">Hydrology</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/10">Invasive species</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/11">Land cover</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/12">Land use</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/18">Mapping</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/13">Modelling</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/3">Phenology</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/14">Pollinators</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/15">Pollution</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/8">Radioecology</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/17">Soil</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/16">Water quality</a></li>
      </ul>
    </div>
    <div class="keyword-add">
      <button class="editor-button add">Add <span class="fa-solid fa-plus" aria-hidden="true"></span></button>
    </div>
  </div>
</div>