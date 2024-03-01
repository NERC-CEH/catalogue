<#import "skeleton.ftlh" as skeleton>
<#assign docroot="documents">
<@skeleton.master title="Search" catalogue=catalogue searching=true containerClass="container-fluid" >
  <div class="search facets-mode" id="search">

    <div class="filters">
        <div class="text">
          <h3 class="facet-heading">Filters</h3>
          <div class="facet-filter">
            <#include "search/_facets.ftlh">
          </div>
        </div>
        <div class="spatial">
          <h3 class="facet-heading">Spatial filter</h3>
          <div class="mapsearch">
          </div>
        </div>
    </div>

    <div>
      <form class="search-form" action="/${docroot}" method="get">
        <div class="input-group">
          <input placeholder="Search the catalogue" name="term" type="text" autocomplete="off" class="form-control">
          <div class="input-group-btn">
            <button tabindex="-1" class="btn btn-success" type="button" aria-label="Search">
              <span class="fa-solid fa-magnifying-glass"></span>
            </button>
          </div>
        </div>
      </form>

      <div class="results">
        <#include "search/_page.ftlh">
      </div>
    </div>

  </div>
</@skeleton.master>
