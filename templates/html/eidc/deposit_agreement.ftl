<#import "../skeleton.ftl" as skeleton>
<#import "/../../functions.tpl" as func>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <#escape x as x?html>
    <div id="metadata" class="metadata depositAgreement">
      <div class="container">
        <#include "_admin.ftl">
        
        <div id="pageTitle">
          <h1>
            <small>EIDC deposit agreement<br></small>
            <#if title?? && title?has_content>${title}</#if>
            <#if (metadata.state == 'draft' || metadata.state == 'pending') >
              <small class="text-info"><br><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></small>
            </#if>
          </h1>
        </div>

        <div class="section section-agreement">
          <div class="section-heading">
            <h2>The agreement</h2>
          </div>
          <div class="section-content">
            <#include "_agreementText.ftl">

            <#if depositor?? || dco??>
            <div class="section-parties">
                <table class="table "><tbody>
                  <#if depositor??>
                  <#list depositor as contact>
                  <tr>
                    <th>Depositor</th>
                    <td>
                      <#if contact.individualName?? && contact.individualName?has_content>
                        ${contact.individualName}
                      </#if>
                    </td>
                    <td>
                      <#if contact.organisationName?? && contact.organisationName?has_content>
                        ${contact.organisationName}
                      </#if>
                    </td>
                    <td>
                      <#if contact.email?? && contact.email?has_content>
                        <a href="mailto:${contact.email}">${contact.email}</a>
                      </#if>
                    </td>
                  </tr>    
                  </#list>
                  </#if>
                  <#if dco??>
                  <#list dco as contact>
                  <tr>
                    <th>For the EIDC</th>
                    <td>
                      <#if contact.individualName?? && contact.individualName?has_content>
                        ${contact.individualName}
                      </#if>
                    </td>
                    <td>
                      <#if contact.organisationName?? && contact.organisationName?has_content>
                        ${contact.organisationName}
                      </#if>
                    </td>
                    <td>
                      <#if contact.email?? && contact.email?has_content>
                        <a href="mailto:${contact.email}">${contact.email}</a>
                      </#if>
                    </td>
                  </tr>    
                  </#list>
                  </#if> 
                  </tbody></table>
            </div>
            </#if>
          </div>
        </div>

        <#if jiraReference?? && jiraReference?has_content>
            <@simplerow "Deposit reference">
              <#if jiraReference?matches("^EIDCHELP\\-\\d+$")>
                <a href="https://jira.ceh.ac.uk/browse/${jiraReference}" target="_blank" rel="noopener noreferrer">${jiraReference}</a>
              <#else>
                  ${jiraReference}
              </#if>
            </@simplerow>
        </#if>

        <div class="section section-foobar">
          <div class="section-content">

          <#if dataCategory?? && dataCategory?has_content>
              <@simplerow "Data category">${dataCategory}</@simplerow>
          </#if>
          </div>
        </div>

        <#if authors??>
          <div class="section section-authors">
            <div class="section-heading">
              <h2>Authors</h2>
            </div>
            <div class="section-content">
            <table class="table table-authors">
            <thead>
              <tr>
                <th>Name</th>
                <th>Affiliation</th>
                <th>Email</th>
                <th>ORCID</th>
              </tr>
            </thead>
            <tbody>
            <#list authors as contact>
            <tr>
              <td>
                <#if contact.individualName?? && contact.individualName?has_content>
                  ${contact.individualName}
                </#if>
              </td>
              <td>
                <#if contact.organisationName?? && contact.organisationName?has_content>
                  ${contact.organisationName}
                </#if>
              </td>
              <td>
                <#if contact.email?? && contact.email?has_content>
                  <a href="mailto:${contact.email}">${contact.email}</a>
                </#if>
              </td>
              <td>
                <#if contact.nameIdentifier?? && contact.nameIdentifier?has_content>
                  ${contact.nameIdentifier?replace("https://orcid.org/","")?replace("http://orcid.org/","")}
                </#if>
              </td>
            </tr>    
            </#list>
            </tbody></table>
            </div>
          </div>
        </#if>


        <#if dataFiles?? || dataFilesOther??>
          <div class="section section-data">
            <div class="section-heading">
              <h2>Files to be deposited</h2>
            </div>
            <div class="section-content">
            <#if dataFiles??>
            <table class="table table-dataFiles">
            <thead>
              <tr>
                <th>Filename</th>
                <th>Format</th>
                <th>Size</th>
              </tr>
            </thead>
            <tbody>
            <#list dataFiles as file>
            <tr>
              <td>
                <#if file.filename?? && file.filename?has_content>
                  ${file.filename}
                </#if>
              </td>
              <td>
                <#if file.format?? && file.format?has_content>
                  ${file.format}
                </#if>
              </td>
              <td>
                <#if file.size?? && file.size?has_content>
                  ${file.size}
                </#if>
              </td>
            </tr>    
            </#list>
            </tbody></table>
            </#if>
            <#if dataFilesOther??>
              <div><@renderLinebreaks dataFilesOther/></div>
            </#if>
            </div>
          </div>
        </#if>

        <#if supportingFiles??>
          <div class="section section-data">
            <div class="section-heading">
              <h2>Supporting documentation</h2>
            </div>
            <div class="section-content">
            <table class="table table-dataFiles">
            <thead>
              <tr>
                <th>Filename</th>
                <th>Content</th>
              </tr>
            </thead>
            <tbody>
            <#list supportingFiles as file>
            <tr>
              <td>
                <#if file.filename?? && file.filename?has_content>
                  ${file.filename}
                </#if>
              </td>
              <td>
                <#if file.content?? && file.content?has_content>
                   <@renderLinebreaks file.content/>
                </#if>
              </td>
            </tr>    
            </#list>
            </tbody></table>
            </div>
          </div>
        </#if>

        <#if licence?? || policies?? || useConstraints??>
        <div class="section section-metadata">
          <div class="section-heading">
            <h2>Licensing &amp; IPR</h2>
          </div>
          <div class="section-content">
            <#if policies?? && policies?has_content>
              <@simplerow "Policies">${policies}</@simplerow>
            </#if>

            <#if licence?? && licence?has_content>
              <#list licence as licence>
              <@simplerow "Licence">${licence.value}</@simplerow>
              </#list>
            </#if>
            
            <#if useConstraints?? && useConstraints?has_content>
              <#list useConstraints as useConstraint>
              <@simplerow "Use constraints">${useConstraint.value}</@simplerow>
              </#list>
            </#if>
          </div>
        </div>
        </#if>


        <div class="section section-metadata">
          <div class="section-heading">
            <h2>Metadata</h2>
          </div>
          <div class="section-content">
            <@simplerow "Record ID">${id}</@simplerow>
            <@simplerow "URL">${uri}</@simplerow>
            <@simplerow "Record last updated">${metadataDateTime?datetime.iso?time?string['d MMMM yyyy, HH:mm']}</@simplerow>
          </div>
        </div>

      </div>
    </div>
  </#escape>
</@skeleton.master>

<#----------MACROS ---------->
<#macro simplerow label>
  <div class="row">
    <div class="col-sm-2 col-xs-12"><div class="view-label">${label}</div></div>
    <div class="col-sm-10 col-xs-12">
      <#nested>
    </div>
  </div>
</#macro>

<#macro renderLinebreaks content>
    ${content?replace("\n", "<br>")}
</#macro>