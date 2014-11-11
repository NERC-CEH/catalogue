<#setting date_format = 'yyyy-MM-dd'>

<div id="section-extent">
<h3><a id="extent"></a>Where/When<!--Extent--></h3>
  <dl class="dl-horizontal">
    <#if boundingBoxes?has_content && boundingBoxes??>
      <dt>Study area</dt>
      <dd>
        <figure title="Map showing the spatial extent of this data resource">
          <div id="studyarea-map" content="${locations?join(',')}"></div>
        </figure>
      </dd>
    </#if>
    <#if temporalExtent?has_content>
      <dt>Temporal extent</dt>
      <dd>
      <#list temporalExtent as extent>
        <div id="temporal-extent" property="dc:temporal" typeof="dc:PeriodOfTime" content="${(extent.begin?date)!''}/${(extent.end?date)!''}">
          <#if extent.begin?has_content>
            <span class="extentBegin">${extent.begin?date}</span>
          <#else>...
          </#if>
 
          &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;

          <#if extent.end?has_content>
            <span class="extentEnd">${extent.end?date}</span>
          <#elseif resourceStatus?has_content && resourceStatus == "onGoing">present
          <#else>...
          </#if>
        </div>
      </#list>
      </dd>
    </#if>
  </dl>            
</div>
