<div id="section-extent">
<h3><a id="extent"></a>Where/When<!--Extent--></h3>
  <dl class="dl-horizontal">
    <#if boundingBoxes?has_content && boundingBoxes??>
      <dt>Study area</dt>
      <dd>
        <figure title="Map showing the spatial extent of this data resource">
          <div id="studyarea-map" property="dc:spatial geo:Geometry" content="${locations?join(',')}"></div>
        </figure>
      </dd>
    </#if>
    <#if temporalExtent?has_content>
      <dt>Temporal extent</dt>
      <dd>
      <span id="temporal-extent" property="dc:temporal" typeof="dc:PeriodOfTime">
        <#list temporalExtent as extent>
          <#if extent_index gt 0>, </#if>
          <#setting date_format = 'yyyy-MM-dd'>
          <#if extent.begin?has_content>
          <span property="dc:start" class="extentBegin">${extent.begin?date}</span>
          <#else>...
          </#if>
          <#if extent.end?has_content>
          &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;
          <span property="dc:end" class="extentEnd">${extent.end?date}</span>
          <#elseif resourceStatus?has_content && resourceStatus == "onGoing">present
          <#else>...
          </#if>
        </#list>
        </span>
      </dd>
    </#if>
  </dl>            
</div>
