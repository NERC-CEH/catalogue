<#if funding?has_content>
    <dt>Funding</dt>
    <dd class="funding">
      <#list funding as fund>
        <span class="award">
          <#if fund.funderName?has_content >
            <span class='funderName'>${fund.funderName}</span>
          </#if>
          <#if fund.awardNumber?has_content >
            <span class='awardNumber'>
              <#if fund.awardURI?has_content>
                <a href="${fund.awardURI}" target="_blank" rel="noopener noreferrer">Award: ${fund.awardNumber}</a>
              <#else>
                Award: ${fund.awardNumber}
              </#if>
            </span>
          </#if>
        </span>
        <#sep><br></#sep>
      </#list>
    </dd>
</#if>
