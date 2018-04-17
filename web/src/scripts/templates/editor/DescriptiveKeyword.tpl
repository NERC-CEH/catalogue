<div class="row">
  <div class="col-sm-1">
    <label class="control-label" for="descriptiveKeyword<%= data.index %>type">Type</label>
  </div>
  <div class="col-sm-11">
    <select data-name="type" class="editor-input type" id="descriptiveKeyword<%= data.index %>type">
      <option value="" selected >- Select Type -</option>
      <option value="CEH Topic">CEH Topic</button>
      <option value="dataCentre">Data Centre</option>
      <option value="discipline">Discipline</button>
      <option value="INSPIRE Theme">INSPIRE Theme</button>
      <option value="instrument">Instrument</option>
      <option value="place">Place</button>
      <option value="project">Project</option>
      <option value="stratum">Stratum</button>
      <option value="taxon">Taxon</option>
      <option value="temporal">Temporal</button>
      <option value="theme">Theme</button>
    </select>
  </div>
</div>
<div class="keywords row col-sm-11 col-sm-offset-1"></div>
<div class="row">
  <div class="col-sm-11 col-sm-offset-1">
    <div class="keyword-add hidden" id="inspireTheme">
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
    <div class="keyword-add hidden" id="cehTopic">
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
    <div class="keyword-add">
      <button class="editor-button add">Add <span class="fas fa-plus" aria-hidden="true"></span></button>
    </div>
  </div>
</div>