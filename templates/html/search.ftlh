<#import "skeleton.ftlh" as skeleton>
<#assign docroot="documents">
<@skeleton.master title="Search" catalogue=catalogue searching=true containerClass="container-fluid" >
  <div class="search facets-mode" id="search">

    <div class="filters">

      <!-- Nav tabs -->
      <div class="search-tabs">
        <ul class="nav nav-tabs" role="tablist">
          <li class="nav-item" role="presentation">
            <a class="nav-link active" id="facet-tab-link" data-bs-toggle="tab" href="#facet-tab" role="tab" aria-controls="facet-tab" aria-selected="true">Filters</a>
          </li>
          <li class="nav-item" role="presentation">
            <a class="nav-link" id="spatial-tab-link" data-bs-toggle="tab" href="#spatial-tab" role="tab" aria-controls="spatial-tab" aria-selected="false">Spatial search</a>
          </li>
        </ul>

        <!-- Tab panes -->
        <div class="tab-content">
          <div class="tab-pane fade show active" id="facet-tab" role="tabpanel" aria-labelledby="facet-tab-link">
            <div class="facet-filter"><#include "search/_facets.ftlh"></div>
          </div>
          <div class="tab-pane fade" id="spatial-tab" role="tabpanel" aria-labelledby="spatial-tab-link">
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
