<#if temporalExtent?has_content>
<h3>Temporal Extent</h3>
  <div id="temporal-extent" property="dc:temporal">
    <#list temporalExtent as extent>
    <#setting date_format = 'yyyy-MM-dd'>
	  <#if extent.begin?has_content>
	  <p class="extentBegin">Begin position: ${extent.begin?date}</p>
	  </#if>
	  <#if extent.end?has_content>
	  <p class="extentEnd">End position: ${extent.end?date}</p>
	  </#if>
    </#list>
  </div>
</#if>
