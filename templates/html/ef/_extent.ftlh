<div id="section-extent">
<h2 id="extent">Where/When</h2>
  <#if boundingBoxes?has_content || geometry?has_content>
    <div id="studyarea-map" class="studyarea-map">
      <#list boundingBoxes as extent>
        <span content="${extent.bounds}" datatype="geoJson"/>
      </#list>
      <#if geometry??>
        <span content="${geometry.value}" datatype="geo:wktLiteral"/>
      </#if>
      <#if uses??>
        <#list uses as facility>
          <#list jena.wkt(facility.href) as wkt>
            <span content="${wkt}" datatype="geo:wktLiteral" href="${facility.href}"/>
          </#list>
        </#list>
      </#if>
    </div>
  </#if>
  <#if lifespan?has_content>
    <p>&nbsp;</p>
    <dl>
      <dt>Lifespan</dt>
      <dd id="temporal-extent">

        <#if lifespan.start?has_content>
          <span class="extentBegin">${lifespan.start?date}</span>
        <#else>...
        </#if>

        &nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;

        <#if lifespan.end?has_content>
          <span class="extentEnd">${lifespan.end?date}</span>
        <#else>present
        </#if>
      </dd>
    </dl>
  </#if>
</div>
