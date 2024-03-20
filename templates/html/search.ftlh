<#import "skeleton.ftlh" as skeleton>
<#assign docroot="documents">
<@skeleton.master title="Search" catalogue=catalogue searching=true containerClass="container-fluid" >
  <div class="search facets-mode" id="search">

    <div class="filters">

<#--      <div class="accordion">-->
<#--        <div class="accordion-item active">-->
<#--          <div>-->
<#--            <div class="accordion-heading">Filters</div>-->
<#--            <div class="accordion-content">-->
<#--              <div class="facet-filter"><#include "search/_facets.ftlh"></div>-->
<#--            </div>-->
<#--          </div>-->
<#--        </div>-->
<#--        <div class="accordion-item accordion-map">-->
<#--          <div>-->
<#--            <div class="accordion-heading">Spatial search</div>-->
<#--            <div class="accordion-content">-->
<#--              <div class="mapsearch"></div>-->
<#--            </div>-->
<#--          </div>-->
<#--        </div>-->
<#--      </div>-->


      <!-- Nav tabs -->
      <div>
        <ul class="nav nav-tabs" role="tablist">
          <li role="presentation" class="tab-search active"><a href="#home" aria-controls="home" role="tab" data-toggle="tab">Filters</a></li>
          <li role="presentation" class="tab-search tab-map"><a href="#profile" aria-controls="profile" role="tab" data-toggle="tab">Map search</a></li>
        </ul>

        <!-- Tab panes -->
        <div class="tab-content">
          <div role="tabpanel" class="tab-pane active" id="home">
            <div class="facet-filter"><#include "search/_facets.ftlh"></div>
          </div>
          <div role="tabpanel" class="tab-pane" id="profile">
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
