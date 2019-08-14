<#if funding?has_content>
    <dt>Funding</dt>
    <dd>
      <#list funding as fund>
        <#assign awardLabel = "">
        <#if fund.awardNumber?has_content && fund.funderName?has_content>
          <#assign awardLabel = fund.funderName + " <span class='awardNumber'>" + fund.awardNumber + "</span>">
        <#elseif fund.awardNumber?has_content >
          <#assign awardLabel = "<span class='awardNumber'>" + fund.awardNumber  + "</span>">
        <#elseif fund.funderName?has_content >
          <#assign awardLabel = "<span class='funderName'>" + fund.funderName  + "</span>">
        </#if>


          <span class="award">
          <#if fund.awardURI?has_content>
            <a href="${fund.awardURI}" target="_blank" rel="noopener noreferrer">${awardLabel}</a>
          <#else>
            ${awardLabel}
          </#if>
          </span>
        <#sep><br></#sep>
      </#list>
    </dd>
</#if>