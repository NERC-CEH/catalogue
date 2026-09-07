<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as b>
<#import "../macro-card.ftlh" as card>

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
          <@card.card item.href item.publicationStatus item.availability item.associationType item.title/>
        </#list>
      </div>
  </#if>
</#macro>
