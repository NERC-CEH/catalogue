<#import "skeleton.ftlh" as skeleton>
<#import "../functions.ftlh" as b>
<#assign docroot="documents">
<@skeleton.master title="Search" catalogue=catalogue searching=true >
  <div class="search facets-mode container" id="search">

    <div class="filters">

      <!-- Nav tabs -->
      <div class="filter-tabs">
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

      <@b.temporaryBlock "2025-10-28">
        <#if catalogue.id == "eidc">
          <div class="m-3 alert alert-warning alert-dismissible fade show" role="alert">
            <strong>Would you like to help shape the future development of our services?</strong><br>
            We're holding an event on 28th Oct at 10.30am to get feedback from our users.
            <a class="alert-link" href="https://events.teams.microsoft.com/event/605973b7-d60d-455b-8e84-46a7982f52c6@7f025136-39f3-4f2c-a3c6-cbb1ce21b36a" target="_blank" rel="nofollow" >Register now</a> to attend.

            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
          </div>
        </#if>
       </@b.temporaryBlock>

      <form class="search-form" action="/${docroot}" method="get">
        <div class="input-group">
          <input placeholder="Search…" name="term" type="text" autocomplete="off" aria-autocomplete="none" class="form-control">
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
