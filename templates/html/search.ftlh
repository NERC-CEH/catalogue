<#import "skeleton.ftlh" as skeleton>
<#assign docroot="documents">

<@skeleton.master title="Search" catalogue=catalogue searching=true containerClass="container-fluid">
  <div class="search facets-mode" id="search">

    <div class="filters-panel">
      <div class="header facet-heading">
        <h2>Filters</h2>
      </div>
      <div class="facet-filter"><#include "search/_facets.ftlh"></div>

      <div class="header map-heading">
        <div class="drawing-control pull-right"><#include "search/_drawing.ftlh"></div>
        <h2>Map Search</h2>
      </div>
      <div class="map-filter openlayers"></div>
    </div>

    <div class="results"><#include "search/_page.ftlh"></div>
    <form class="search-form" action="/${docroot}" method="get">
      <div class="input-group">
        <input placeholder="Search the catalogue" name="term" type="text" autocomplete="off" class="form-control">
        <div class="input-group-btn">
          <button tabindex="-1" class="btn btn-success" type="button" aria-label="Search">
            <span class="fas fa-search"></span>
          </button>
        </div>
      </div>
    </form>

  </div>
</@skeleton.master>
