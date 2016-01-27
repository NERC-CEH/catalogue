<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="descriptiveKeyword<%= data.index %>type">Type</label>
  </div>
  <div class="col-sm-3 col-lg-3">
    <select data-name="type" class="editor-input type" id="descriptiveKeyword<%= data.index %>type">
      <option value="" selected >- Select Type -</option>
      <option value="discipline">Discipline</button>
      <option value="place">Place</button>
      <option value="stratum">Stratum</button>
      <option value="temporal">Temporal</button>
      <option value="theme">Theme</button>
    </select>
  </div>
</div>
<h5>Keywords</h5>
<div class="keywords"></div>
<div class="row">
  <div class="col-sm-5 col-lg-5">
    <div class="dropdown hidden" id="inspireTheme">
      <button class="editor-button dropdown-toggle" type="button" id="dropdownInspireMenu" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
        Choose keyword from the INSPIRE vocabulary
        <span class="caret"></span>
      </button>
      <ul class="dropdown-menu predefined" aria-labelledby="dropdownInspireMenu">
        <li><a href="http://inspire.ec.europa.eu/theme/ad">Addresses</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/au">Administrative Units</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/af">Agricultural and Aquaculture Facilities</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/am">Area Management Restriction Regulation Zones and Reporting units</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/ac">Atmospheric Conditions</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/ac-mf">Atmospheric Conditions and meteorological geographical features</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/br">Bio-geographical Regions</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/bu">Buildings</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/cp">Cadastral Parcels</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/rs">Coordinate reference systems</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/el">Elevation</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/er">Energy Resources</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/ef">Environmental Monitoring Facilities</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/gg">Geographical grid systems</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/gn">Geographical Names</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/ge">Geology</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/hb">Habitats and Biotopes</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/hh">Human Health and Safety</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/hy">Hydrography</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/lc">Land Cover</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/lu">Land Use</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/mf">Meteorological geographical features</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/mr">Mineral Resources</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/nz">Natural Risk Zones</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/of">Oceanographic Geographical Features</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/oi">Orthoimagery</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/pd">Population Distribution - Demography</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/pf">Production and Industrial Facilities</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/ps">Protected Sites</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/sr">Sea Regions</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/so">Soil</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/sd">Species Distribution</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/su">Statistical Units</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/tn">Transport Networks</a></li>
        <li><a href="http://inspire.ec.europa.eu/theme/us">Utility and Governmental Services</a></li>
      </ul>
    </div>
    <div class="dropdown hidden" id="cehTopic">
      <button class="editor-button dropdown-toggle" type="button" id="dropdownCehTopicMenu" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
        Choose keyword from CEH themes
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
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/17">Soil</a></li>
        <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/16">Water quality</a></li>
      </ul>
    </div>
    <button class="editor-button add">Add <span class="glyphicon glyphicon-plus" aria-hidden="true"></span></button>
  </div>
</div>
<h5>Thesaurus</h5>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="descriptiveKeyword<%= data.index %>title">Title</label>
  </div>
  <div class="col-sm-11 col-lg-11">
      <input data-name='title' class="editor-input" id="descriptiveKeyword<%= data.index %>title" value="<%= data.thesaurusName.title %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="descriptiveKeyword<%= data.index %>Date">Date</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <input data-name='date' class="editor-input date" id="descriptiveKeyword<%= data.index %>Date" value="<%= data.thesaurusName.date %>">
  </div>
  <div class="col-sm-2 col-lg-2">
    <label class="control-label" for="descriptiveKeyword<%= data.index %>dateType">Date Type</label>
  </div>
  <div class="col-sm-3 col-lg-3">
    <select data-name="dateType" class="editor-input dateType" id="descriptiveKeyword<%= data.index %>dateType">
      <option value="" selected >- Select Date Type -</option>
      <option value="creation">Creation</button>
      <option value="publication">Publication</button>
      <option value="revision">Revision</button>
    </select>
  </div>
</div>
