<#import "skeleton.html.tpl" as skeleton>
<#assign docroot="documents">

<@skeleton.master title="Search">
  <div class="search" id="search">
    <div class="spatial-search">
      <div class="openlayers"></div>
      <div class="mapsearch-toggle"><input type="checkbox">Use the Map to Search?</div>
    </div>
    <div class="text-search">
      <form class="search-form" action="/${docroot}" method="get">
        <div class="input-group">
          <@facets/>
          <input name="term" type="text" class="form-control">
          <div class="input-group-btn">
            <button tabindex="-1" class="btn btn-success" type="button">
              <span class="glyphicon glyphicon-search"></span>
            </button>
          </div>
        </div>
      </form>
      <div class="results">
        <#include "_searchPage.html.tpl">
      </div>
    </div>
  </div>
</@skeleton.master>

<#--
  The actual facets which we are going to use have not been fully decided yet.
  This macro will generate an example set
-->
<#macro facets>
  <div class="input-group-btn">
    <button tabindex="-1" data-toggle="dropdown" class="btn btn-default dropdown-toggle" type="button">
      Hop Topics
      <span class="caret"></span>
    </button>
    <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">
      <li><a href="#">Agriculture</a></li>
      <li><a href="#">Climate</a>
          <ul>
            <li><a href="#">Climate change</a></li>
          </ul>
      </li>
      <li><a href="#">Modelling</a>
        <ul>
          <li><a href="#">Integrated Ecosystem Modelling</a></li>
        </ul>
      </li>
      <li><a href="#">Natural capital</a>
        <ul>
          <li><a href="#">Ecosystem services</a></li>
          <li><a href="#">Pollinators</a></li>
          <li><a href="#">Biodiversity</a></li>
        </ul>
      </li>
      <li><a href="#">Natural hazards</a>
        <ul>
          <li><a href="#">Invasive species</a></li>
          <li><a href="#">Flood</a></li>
          <li><a href="#">Drought</a></li>
        </ul>
      </li>
      <li><a href="#">Pollution</a>
        <ul>
          <li><a href="#">Atmospheric pollution</a></li>
          <li><a href="#">Organic pollutants</a></li>
          <li><a href="#">Radionuclides</a></li>
        </ul>
      </li>
      <li><a href="#">Soil</a></li>
      <li><a href="#">Hydrology</a></li>
      <li><a href="#">Water quality</a></li>
      <li><a href="#">Survey and Monitoring</a>
        <ul>
          <li><a href="#">Long-term monitoring</a></li>
          <li><a href="#">Real time monitoring</a></li>
        </ul>
      </li>
      <li><a href="#">Ecosystems and Landscapes</a>
      <ul>
          <li><a href="#">Habitat fragmentation</a></li>
          <li><a href="#">Land cover</a></li>
          <li><a href="#">Land use</a></li>
        </ul>
      </li>
    </ul>
  </div>
</#macro> 