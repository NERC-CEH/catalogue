<#import "skeleton.html.tpl" as skeleton>
<#assign docroot="documents">

<@skeleton.master title="Search" catalogue=catalogue searching=true>
  <div class="search facets-mode" id="search">

    <div class="filters-panel">
      <div class="header facet-heading">
        <h3>FILTERS</h3>
      </div>
      <div class="facet-filter"><#include "search/_facets.html.tpl"></div>

      <div class="header map-heading">
        <div class="drawing-control pull-right"><#include "search/_drawing.html.tpl"></div>
        <h3>MAP SEARCH</h3>
      </div>
      <div class="map-filter openlayers"></div>
    </div>

    <div class="results"><#include "search/_page.html.tpl"></div>
    <form class="search-form" action="/${docroot}" method="get">
      <div class="input-group">
        <input placeholder="Search the catalogue" name="term" type="text" autocomplete="off" class="form-control">
        <div class="input-group-btn">
          <button tabindex="-1" class="btn btn-success" type="button">
            <span class="fa fa-search"></span>
          </button>
        </div>
      </div>
    </form>

  </div>
</@skeleton.master>
