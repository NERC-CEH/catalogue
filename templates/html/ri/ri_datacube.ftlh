<#import "../skeleton.ftlh" as skeleton>

<#assign rel_all=jena.allRelatedRecords(uri)>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <div id="metadata" class="metadata ris-metadata">
      <div class="container">
        <#include "_admin.ftlh">


        <#if title?? && title?has_content>
          <div id="pageTitle">
            <h1>${title}</h1>
          </div>
        </#if>

        <#if infrastructureClass??><@displaykey infrastructureClass "Infrastructure class" /></#if>
        <#if infrastructureCategory??><@displaykey infrastructureCategory "Infrastructure category" /></#if>
        <#if purpose??><@displaykey purpose "Purpose" /></#if>
        <#if capabilities??><@displaykey capabilities "Capabilities" /></#if>
        <#if lifecycle??><@displaykey lifecycle "Lifecycle" /></#if>
        <#if uniqueness??><@displaykey uniqueness "Uniqueness" /></#if>
        <#if partners??><@displaykey partners "Partners" /></#if>
        <#if access??><@displaykey access "Access" /></#if>
        <#if locationText??><@displaykey locationText "Location" /></#if>
        <#if userCosts??><@displaykey userCosts "User costs"/></#if>
        <#if fundingSources??><@displaykey fundingSources "Funding sources"/></#if>
        <#if scienceArea??><@displaykey scienceArea "Science area" /></#if>

        <#if users?? >
          <#assign numUsers = users?size>
          <div class="key-value">
            <div>User<#if numUsers gte 2>s</#if></div>
            <div><ul>
              <#list users as user>
                <li>${user}</li>
              </#list>
            </ul></div>
          </div>
        </#if>

        <#if owners?? >
          <#assign numOwners = owners?size>
          <div class="key-value">
            <div>Owner<#if numOwners gte 2>s</#if> </div>
            <div>
              <#list owners as owner>
                  <#if owner.individualName??>${owner.individualName}</#if>
                  <#if owner.organisationName?has_content><br>${owner.organisationName}</#if>
                  <#if owner.address?has_content>
                    <#if owner.address.city?has_content>, ${owner.address.city}</#if>
                  </#if>
              </#list>
            </div>
          </div>
        </#if>

      

        <#if rel_all?has_content && rel_all?size gt 0>
          <div class="relatedRecords>
            <h2>Related</h2>
          </div>
        <#else>
          No related records
        </#if>
 


        <#assign lastUpdated = metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']>
        <@displaykey lastUpdated "Last updated" />


    </div>
</@skeleton.master>

<#macro displaykey key label>
  <div class="key-value">
    <div>${label}</div>
    <div>${key}</div>
  </div>
</#macro>