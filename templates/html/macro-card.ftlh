<#macro card address publicationStatus availability type title icon='fa-globe' external=false >
  
  <#assign target = "">
  <#if external=true>
			<#assign target=" target='_blank' rel='noopener noreferrer'"?no_esc>
	</#if>

  <#assign cardClass = " ukcehcard-" + publicationStatus?lower_case >
  <#if availability?has_content && availability != "Unknown">
   <#assign cardClass += " ukcehcard-" + availability?lower_case>
	</#if>

  <#assign visible = false>
  <#if publicationStatus=="published" || permission.userCanEdit(id)>
     <#assign visible = true>
  </#if>

  <#if visible>
    <#if address?has_content && address?matches("^http(|s)://.+")>
      <a class="ukcehcard ${cardClass}" href="${address}" ${target}>

        <div>
          <span  class="ukcehcard-icon"> <i class="fa-solid ${icon}"></i></span>
        </div>
        <div class="ukcehcard-content">
          <div class="ukcehcard-type"><span>${publicationStatus}</span>${type}</div>
          <div class="ukcehcard-title">${title}</div>
        </div>
      </a>
    </#if>
  </#if>
</#macro>
