<#import "skeleton.ftlh" as skeleton>
<@skeleton.master title="Validation Page" catalogue=catalogues.defaultCatalogue()>
  <div class="container">
    <h1>Output format validation report</h1>

    <#list validatorResults as result>
      <h2>${result.name}</h2>
      <div class="row chart">
        <div class="col-md-6 ">
          <canvas></canvas>
        </div>
        <div class="col-md-6">
          <ul class="nav nav-tabs" role="tabs">
            <#list result.states as state>
              <li role="presentation" <#if state_index = 0>class="active"</#if>>
                <a href="#${result.name}_${state.level}" role="tab" data-bs-toggle="tab">
                  <span class="badge badge-${state.level}">${state.documents?size}</span> <span class="legend-label">${state.level.label}</span>
                </a>
              </li>
            </#list>
          </ul>

          <div class="tab-content validation-results">
            <#list result.states as state>
              <div role="tabpanel" class="tab-pane <#if state_index = 0>active</#if>" id="${result.name}_${state.level}">
                <#list state.documents as document>
                  <p><a href="/documents/${document}">${document}</a></p>
                </#list>
              </div>
            </#list>
          </div>
        </div>
      </div>
    </#list>

    <#if failed?has_content>
      <hr>
      <div class="panel panel-danger">
        <div class="panel-heading">Records which completely failed</div>
        <div class="panel-body">
          <#list failed as document>
            <p><a href="/documents/${document}">${document}</a></p>
          </#list>
        </div>
      </div>
    </#if>
  </div>
</@skeleton.master>
