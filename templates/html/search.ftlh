<#import "skeleton.ftlh" as skeleton>
<#import "../functions.ftlh" as func>
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

      <@func.expiringBanner expiry="2026-10-31" catalogueId=catalogue.id shownOn=["eidc"]>
        <strong>Programmatic access changing on 5 October 2026</strong><br>
        From 5 October 2026, basic authentication for data downloads and catalogue requests will be replaced by Personal Access Tokens (PATs). PATs are available now.<br>
        This only affects users downloading data using tools such as Wget and cURL. Catalogue logins are unaffected.<br>
        <a class="alert-link" href="https://eidc.ac.uk/help/getdata/downloadData#programmaticAccess" target="_blank" rel="noopener noreferrer">Click here for more information</a>.
        Please contact <a class="alert-link" href="mailto:eidc@ceh.ac.uk">eidc@ceh.ac.uk</a> if you have any questions.
      </@func.expiringBanner>

      <form class="search-form" action="/${docroot}" method="get">
        <div class="input-group">
          <input placeholder="Search…" name="term" type="text" autocomplete="off" aria-autocomplete="none" class="form-control">
          <#if semanticEnabled>
          <#--
            Semantic search takes a sentence rather than keywords, so it gets a
            multi row textarea instead of the single line input. Both controls are
            rendered and SearchFormView swaps between them, disabling the one which
            is not in use so that only one 'term' is ever submitted.
          -->
          <textarea placeholder="Describe what you are looking for, in your own words…" name="term" rows="4" autocomplete="off" aria-label="Search" class="form-control d-none" disabled></textarea>
          </#if>
          <div class="input-group-btn">
            <button class="btn" type="submit" aria-label="Search">
              <span class="fa-solid fa-magnifying-glass"></span>
              <#if semanticEnabled><span class="search-button-label">Search</span></#if>
            </button>
          </div>
        </div>
        <#if semanticEnabled>
        <div class="mt-2">
          <div class="form-check form-switch">
            <input class="form-check-input" type="checkbox" id="semanticSearch" name="semantic" value="true">
            <label class="form-check-label" for="semanticSearch">Semantic search</label>
          </div>
        </div>
        </#if>
      </form>

      <div class="results">
        <#include "search/_page.ftlh">
      </div>
    </div>
  </div>

</@skeleton.master>
