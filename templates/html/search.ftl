<#import "skeleton.ftl" as skeleton>
<#assign docroot="documents">

<@skeleton.master title="Search" catalogue=catalogue searching=true containerClass="container-fluid">
  <div class="search facets-mode" id="search">

    <div class="filters-panel">
      <div class="header facet-heading">
        <h3>Filters</h3>
      </div>
      <div class="facet-filter"><#include "search/_facets.ftl"></div>

      <div class="map-search">
        <div class="header map-heading">
          <div class="drawing-control pull-right"><#include "search/_drawing.ftl"></div>
          <h3>Map Search</h3>
        </div>
        <div class="map-filter openlayers"></div>
      </div>
    </div>

    <div class="results"><#include "search/_page.ftl"></div>
    <form class="search-form" action="/${docroot}" method="get">
      <div class="input-group">
        <input placeholder="Search the catalogue" name="term" type="text" autocomplete="off" class="form-control">
        <div class="input-group-btn">
          <button tabindex="-1" class="btn btn-success" type="button">
            <span class="fas fa-search"></span>
          </button>
        </div>
      </div>
    </form>

  </div>
</@skeleton.master>
