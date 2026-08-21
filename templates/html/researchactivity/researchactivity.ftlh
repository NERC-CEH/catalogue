<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as b>


<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) footer=true>
  <div id="metadata" class="metadata">
    <section class="section">
      <div class="container">
       <@admin/>

        <#if title?? && title?has_content>
          <div id="pageTitle">
            <div>Research activity</div>
            <h1>
            <#if (metadata.state == 'draft' || metadata.state == 'pending') >
              <small class="text-danger fw-bold">${codes.lookup('publication.state', metadata.state)?upper_case!''}</small>
            </#if>
            ${title}
            </h1>
          </div>
        </#if>

        <#if description??>
          <div class="description">
            <div class="clearfix description-text">
              <@browseGraphic />
              <@b.linebreaksAndLinks description/>
            </div>
          </div>
        </#if>

      <@displayContributors />
      <@funding />
      <@displayOutputs />

      </div>
    </section>

  </div>
</@skeleton.master>

<#macro admin>
  <#if permission.userCanEdit(id)>
  <div class="row d-print-none" id="adminPanel">
      <div class="text-end adminToolbar" role="toolbar">
        <div class="btn-group btn-group-sm">
          <button type="button" class="btn btn-outline-secondary btn-wide edit-control"  data-document-type="${metadata.documentType}">Edit</button>
          <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
          <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            <span class="caret"></span>
            <span class="visually-hidden">Toggle Dropdown</span>
          </button>
          <ul class="dropdown-menu dropdown-menu-end">
            <li><a class="dropdown-item" href="/documents/${id}/permission"><i class="fa-solid fa-users"></i> Permissions</a></li>
            <li><a class="dropdown-item" href="/documents/${id}/publication"><i class="fa-solid fa-eye"></i> Publication status</a></li>
            <li role="separator" class="divider"></li>
            <li><a href="/documents/${id}/catalogue" class="dropdown-item catalogue-control"><i class="fa-solid fa-right-from-bracket"></i> Move to a different catalogue</a></li>
          </ul>
          </#if>
        </div>
      </div>
  </div>
  </#if>
</#macro>

<#macro browseGraphic>
  <#local images = []>

  <#if onlineResources?? && onlineResources?has_content>
    <#list onlineResources?filter(o -> o.function=='browseGraphic')?filter(o -> o.url?lower_case?matches('.*\\.(png|jpg|jpeg)$')) as item>
      <#local images = images + [item]>
    </#list>
  </#if>

  <#if images?has_content>
    <img class="browseGraphic" src="${images?first.url!''}" alt="${images?first.name!'logo'}"/>
  </#if>
</#macro>

<#macro displayContributors>
   <#if contributors?has_content>
    <h2>Contributors</h2>

    <div class="contributors">
      <#list contributors as c >
        <div class="contributor mb-4">
          <div>${c.fullName}</div>
          <div>${c.organisationName}</div>
        </div>
      </#list>
    </div>
   </#if>
</#macro>

<#macro funding>
  <#if funders?has_content>
    <div class="funding">
      <h2>Funding</h2>
      <#list funders as fund>
        <div class="funder mb-4">
          ${fund.organisationName}
            <div class="awards">
              <#list fund.awards as award>
                <#-- The property is awardURI, not awardUri: a mismatched name here is
                     silently missing rather than an error, so the award disappears. -->
                <#local awardLabel = "">
                <#if award.awardTitle?has_content>
                  <#local awardLabel = award.awardTitle>
                  <#if award.awardNumber?has_content>
                    <#local awardLabel = awardLabel + ' (' + award.awardNumber + ')'>
                  </#if>
                <#elseif award.awardNumber?has_content>
                  <#local awardLabel = award.awardNumber>
                <#else>
                  <#local awardLabel = award.awardURI>
                </#if>

                <#if awardLabel?has_content>
                  <div class="award mb-1">
                    <#if award.awardURI?has_content>
                        <div>
                          <a href="${award.awardURI}" target="_blank" rel="noopener noreferrer">${awardLabel}</a>
                        </div>
                    <#else>
                        <div>${awardLabel}</div>
                    </#if>
                  </div>
                </#if>
              </#list>
            </div>
        </div>
      </#list>
    </div>
  </#if>
</#macro>

<#macro displayOutputs>
  <#if relHasOutput?has_content>
      <h2>Outputs</h2>
      <div class="ukcehcards">
        <#list relHasOutput as item>
          <@card item.href item.publicationStatus item.availability item.associationType item.title/>
        </#list>
      </div>
  </#if>
</#macro>

<#macro card address publicationStatus availability associationType title external=false >
  <#local target = "">
  <#if external=true>
    <#local target=" target='_blank' rel='noopener noreferrer'"?no_esc>
  </#if>

  <#local cardClass = " ukcehcard-" + publicationStatus?lower_case >
  <#if availability?has_content && availability != "Unknown">
   <#local cardClass = cardClass + " ukcehcard-" + availability?lower_case>
	</#if>

  <#local visible = false>
  <#if publicationStatus=="published" || permission.userCanEdit(id)>
     <#local visible = true>
  </#if>

  <#local type=associationType icon="" >
  <#if associationType = 'dataset' ||  associationType = 'nonGeographicDataset' ||  associationType = 'signpost'>
    <#local type="Dataset" icon="fa-solid fa-chart-column" >
  <#elseif associationType = "series" || associationType = "collection" || associationType = "aggregate">
    <#local type="Data collection" icon="fa-layer-group" >
  <#elseif associationType = "model">
    <#local type="Model" icon="fa-list-check" >
  <#elseif associationType = "software" || associationType = "application">
    <#local type="Software" icon="fa-code" >
  <#elseif associationType = "computationalNotebook">
    <#local type="Notebook" icon="fa-book-open" >
  <#elseif associationType = "method">
    <#local type="Method" icon="fa-list-check">
  <#elseif associationType = "service">
    <#local type="Web service" icon="fa-location-arrow" >
  <#elseif associationType = "monitoringFacility">
    <#local type="Monitoring facility" icon="fa-map-pin" >
  </#if>

  <#if visible>
    <#if address?has_content && address?matches("^http(|s)://.+")>
      <a class="ukcehcard new${cardClass}" href="${address}" ${target}>

        <div>
          <span class="ukcehcard-icon"><i class="fa-solid ${icon}"></i></span>
        </div>
        <div class="ukcehcard-content">
          <div class="ukcehcard-type"><span>${publicationStatus}</span>${type}</div>
          <div class="ukcehcard-title">${title}</div>
        </div>
      </a>
    </#if>
  </#if>
</#macro>
