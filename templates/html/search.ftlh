<#import "skeleton.ftlh" as skeleton>
<#assign docroot="documents">
<@skeleton.master title="Search" catalogue=catalogue searching=true containerClass="container-fluid" >
  <div class="search facets-mode" id="search">

    <div class="filters">

      <!-- Nav tabs -->
      <div class="search-tabs">
        <ul class="nav nav-tabs" role="tablist">
          <li class="nav-item" role="presentation">
            <a class="nav-link active" href="#facet-tab" aria-controls="facet-tab" role="tab" data-bs-toggle="tab">Filters</a>
          </li>
          <li role="presentation" class="nav-item search-tab map-tab"><a class="nav-link" href="#spatial-tab" aria-controls="spatial-tab" role="tab" data-bs-toggle="tab">Spatial search</a></li>
        </ul>

        <!-- Tab panes -->
        <div class="tab-content">
          <div role="tabpanel" class="tab-pane active" id="facet-tab">
            <div class="facet-filter"><#include "search/_facets.ftlh"></div>
          </div>
          <div role="tabpanel" class="tab-pane" id="spatial-tab">
            <div class="mapsearch"></div>
          </div>
        </div>
      </div>

    </div>

    <div>
      <form class="search-form" action="/${docroot}" method="get">
        <div class="input-group">
          <input placeholder="Search the catalogue" name="term" type="text" autocomplete="off" class="form-control">
          <div class="input-group-btn">
            <button tabindex="-1" class="btn" type="button" aria-label="Search">
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
