<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as f>

<#if useConstraints?has_content>
  <#assign licences = f.filter(useConstraints, "code", "license")>
</#if>
<#if responsibleParties?has_content>
  <#assign
    owners = f.filter(responsibleParties, "role", "owner")
    contacts = f.filter(responsibleParties, "role", "pointOfContact")
    >
</#if>

<#assign
  rel_memberOf = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")
  rel_hasMember = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")
  rel_related = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo") + jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo")
  relationships = rel_memberOf + rel_hasMember + rel_related
>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

  <div class="codeDocument">
    <#include "_admin.ftlh">

    <div class="section section-grey">
      <div class="container">
        <h1>
          <#if type?? && type?has_content>
            <small>${codes.lookup('metadata.resourceType', type)!''}</small><br>
          </#if>
          ${title}
          <#if (metadata.state == 'draft' || metadata.state == 'pending') >
            <small class="text-danger"><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></small>
          </#if>
        </h1>
        <#if description?? && description?has_content>
          <div><@linebreaks description /></div>
        </#if>
      </div>
    </div>

    <div class="section">
      <div class="container">

        <div class="grid">

          <div class="grid--one">
            <#if assetType?? && assetType?has_content>
              <@displaySimple "Type" assetType />
            </#if>
          </div>

          <div class="grid--two">
            <#if (owners?? && owners?has_content) || (contacts?? && contacts?has_content)>
            <div>
              <#if owners?? && owners?has_content>
                  <@displayContact "Owner" owners />
              </#if>

              <#if contacts?? && contacts?has_content>
                  <@displayContact "Contact" contacts />
              </#if>
             </div>
             </#if>

          </div>

          <div class="grid--three">

            <#if version?? && version?has_content>
              <@displaySimple "Version" version />
            </#if>

            <#if masterUrl?? && masterUrl?has_content>
              <h3>Master url</h3>
              <p>
                <#if masterUrl?matches("^http.+")>
                  <a href="${masterUrl} target="_blank" rel="noreferrer noopener">${masterUrl}</a>
                <#else>
                  ${masterUrl}
                </#if>
              </p>
            </#if>

            <#if primaryLanguage?? && primaryLanguage?has_content>
              <@displaySimple "Primary language" primaryLanguage />
            </#if>
            <#if secondaryLanguage?? && secondaryLanguage?has_content>
              <@displaySimple "Secondary language" secondaryLanguage />
            </#if>

            <#if inputs?? && inputs?has_content>
              <@displaySimpleList "Inputs" inputs />
            </#if>
            <#if outputs?? && outputs?has_content>
              <@displaySimpleList "Outputs" outputs />
            </#if>
            <#if packages?? && packages?has_content>
              <@displaySimpleList "Packages" packages />
            </#if>

            <#if review?? && review?has_content>
              <h3>Review</h3>
              <dl class="dl-horizontal">
              <#list review as r>
                <#if (r.reviewDate?? && r.reviewDate?has_content) || (r.reviewProcess?? && r.reviewProcess?has_content)>
                  <dt>
                    <#if r.reviewDate?? && r.reviewDate?has_content>
                      ${r.reviewDate?date?string['dd MMMM yyyy']}
                    <#else>
                      ---
                    </#if>
                  </dt>
                  <dd>
                    <#if r.reviewProcess?? && r.reviewProcess?has_content>
                      ${r.reviewProcess}
                    <#else>
                      ---
                    </#if>
                  </dd>
                </#if>
              </#list>
              </dl>
            </#if>



            <#if boundingBoxes?has_content>
              <div>
                <h3>Spatial extent</h3>
                  <div id="studyarea-map">
                    <#list boundingBoxes as extent>
                      <span content="${extent.bounds}" datatype="geoJson"/>
                    </#list>
                  </div>
              </div>
            </#if>

            <#if temporalExtents?has_content>
              <div>
              <h3>Temporal extent</h3>
                <#list temporalExtents as extent>
                    <#if extent.begin?has_content>
                      <span class="extentBegin">${extent.begin?date}</span>
                    <#else>
                      &hellip;
                    </#if>
                    &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;
                    <#if extent.end?has_content>
                      <span class="extentEnd">${extent.end?date}</span>
                    <#else>
                      &hellip;
                    </#if>
                    <#sep><br>&nbsp;&nbsp;&nbsp;&nbsp;and <br></#sep>
                </#list>
              </div>
            </#if>

            <#if relationships?has_content>
              <h3>Related record<#if relationships?size gt 1>s</#if></h3>

              <#if rel_memberOf?has_content>
                <@displayRelations rel_memberOf "This notebook is used in" />
              </#if>

              <#if rel_hasMember?has_content>
                <@displayRelations rel_hasMember "This project uses" />
              </#if>

              <#if rel_related?has_content>
                <@displayRelations rel_related "Other related records" />
              </#if>

            </#if>

            <#if keywords?has_content>
            <h3>Keyword<#if keywords?size gt 1>s</#if></h3>
            <div class="keywords">
              <#list keywords?sort_by("value") as keyword>
                <#if keyword.uri?has_content>
                  <a href="${keyword.uri}" target="_blank" rel="noopener noreferrer">${keyword.value?trim}</a><#sep>,&nbsp;</#sep>
                <#else>
                  ${keyword.value?trim}<#sep>,&nbsp;</#sep>
                </#if>
              </#list>
            </div>
            </#if>

            <#if inspireThemes?has_content>
            <h3>INSPIRE theme<#if inspireThemes?size gt 1>s</#if></h3>
            <div class="inspireThemes">
              <#list inspireThemes?sort_by("theme") as theme>
                <#if theme.uri?has_content>
                  <a href="${theme.uri}" target="_blank" rel="noopener noreferrer">${theme.theme?trim}</a><#sep><br></#sep>
                <#else>
                  ${theme.value?trim}<#sep><br></#sep>
                </#if>
              </#list>
            </div>
            </#if>


            <@displayNest "Last updated">
              ${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}
            </@displayNest>

          </div>
        </div>

        </div>
    </div>

  </div>

<#-- ########## MACROS ########## -->
<#macro displayNest key>
  <div>
    <h3>${key}</h3>
    <#nested>
  </div>
</#macro>

<#macro displaySimple key value>
   <@displayNest key>
      <p>${value}</p>
   </@displayNest>
</#macro>

<#macro displaySimpleList key value>
   <@displayNest key>
      <#list value as item>
        <#if item?matches("^http(s)?://.+$")>
          <#local foo = "<a href='" + item + "'>" + item + "</a>">
        <#else>
        <#local foo = item>
        </#if>
        <div>${foo?no_esc}</div>
      </#list>
   </@displayNest>
</#macro>

<#macro displayRelations key label>
  <dl class="dl-horizontal"><dt>${label}</dt><dd>
  <#list key as item>
      <a href=" ${item.href}">${item.title}</a>
    <#sep><br></#sep>
  </#list></dd></dl>
</#macro>

<#macro linebreaks value>
  <#if value?has_content>
    <#list value?split("\n") as line>
        ${line}<#sep><br></#sep>
    </#list>
  </#if>
</#macro>

<#macro displayContact key value>
    <h3>${key}<#if value?size gt 1>s</#if></h3>
    <#list value as item>
      <div>
        <#if item.individualName?? && item.individualName?has_content>${item.individualName}<br></#if>
        <#if item.organisationName?? && item.organisationName?has_content>${item.organisationName}<br></#if>
        <#if item.email?? && item.email?has_content><a href="mailto:${item.email}">${item.email}</a></#if>
        </div>
    </#list>
</#macro>
</@skeleton.master>

