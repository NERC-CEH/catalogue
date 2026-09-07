<#import "../macro-card.ftlh" as card>
<#assign
	otherSupplemental = nonRelatedDatasets
	apps = browsingApps
>

<div class="py-3">

  <#if title?? && title?has_content>
    <#include "_title.ftlh">
  </#if>

  <#include "_aggregateAccessButtons.ftlh">

  <#if description??>
    <#include "_description.ftlh">
  </#if>

  <#if apps?? && apps?size gte 1>
    <#include "_spatialExplorer.ftlh">
  </#if>

  <#if relHasPart?has_content && relHasPart?size gt 0 >
    <div id="segment-members">
      <h2>
      This collection contains <#if relHasPart?size gt 1>these<#else>this</#if> resource<#if relHasPart?size gt 1>s</#if></h2>

      <div class="ukcehcards">
        <#list relHasPart>
            <#items as item>
              <#if item.associationType = 'dataset' ||  item.associationType = 'nonGeographicDataset' ||  item.associationType = 'signpost'>
                <#assign type="Dataset" icon="fa-solid fa-chart-column" >
              <#elseif item.associationType = "series" || item.associationType = "collection" || item.associationType = "aggregate">
                <#assign type="Data collection" icon="fa-layer-group" >
              <#elseif item.associationType = "model">
                <#assign type="Model" icon="fa-list-check" >
              <#elseif item.associationType = "software" || item.associationType = "application">
                <#assign type="Software" icon="fa-code" >
              <#elseif item.associationType = "computationalNotebook">
                <#assign type="Notebook" icon="fa-book-open" >
              <#elseif item.associationType = "method">
                <#assign type="Method" icon="fa-list-check">
              <#elseif item.associationType = "service">
                <#assign type="Web service" icon="fa-location-arrow" >
              <#else>
                <#assign type=item.associationType icon="" >
              </#if>

              <@card.card item.href item.publicationStatus item.availability type item.title icon/>
            </#items>
        </#list>

        <#if apps?has_content && apps?size gt 0>
          <#list apps as app>
            <@card.card app.url '' '' 'Application' app.name 'fa-solar-panel'/>
          </#list>
        </#if>
      </div>
    </div>
  </#if>

  <#if (relRelation?has_content && relHasPart?size gt 0) || (otherSupplemental?has_content && otherSupplemental?size gt 0) >
    <div id="segment-related" class="mt-5">

      <h3 class="my-3">Other related resources</h3>

      <div class="ukcehcards ukcehcards-small">
        <#list relRelation>
          <#items as item>
            <#if item.associationType = 'dataset' ||  item.associationType = 'nonGeographicDataset' ||  item.associationType = 'signpost'>
              <#assign type="Dataset" icon="fa-solid fa-chart-column" >
            <#elseif item.associationType = "series" || item.associationType = "collection" || item.associationType = "aggregate">
              <#assign type="Data collection" icon="fa-layer-group" >
            <#elseif item.associationType = "model">
              <#assign type="Model" icon="fa-list-check" >
            <#elseif item.associationType = "software">
              <#assign type="Software" icon="fa-code" >
            <#elseif item.associationType = "computationalNotebook">
              <#assign type="Notebook" icon="fa-book-open" >
            <#elseif item.associationType = "method">
              <#assign type="Method" icon="fa-list-check">
            <#elseif item.associationType = "service">
              <#assign type="Web service" icon="fa-location-arrow" >
            <#else>
              <#assign type=item.associationType  >
            </#if>

            <@card.card item.href item.publicationStatus item.availability type item.title icon/>
          </#items>
        </#list>

        <#list otherSupplemental>
          <#items as item>
            <@card.card item.url "published" "available" "Website" item.name "fa-globe" true />
          </#items>
        </#list>

      </div>
    </div>
  </#if>

  <#if relPartOf?has_content && relPartOf?size gt 0 >
    <h2>This ${recordType} is included in the following collection<#if relPartOf?size gt 1>s</#if></h2>
    <div class="ukcehcards">
      <#list relPartOf?sort_by("title") as item>
        <@card.card item.href item.publicationStatus item.availability 'Data collection' item.title/>
      </#list>
    </div>
  </#if>

</div>