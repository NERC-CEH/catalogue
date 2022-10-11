<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as func>
<#import "../blocks.ftlh" as b>
<#import "macros.ftlh" as m>

<#if onlineResources?? && onlineResources?has_content>
  <#assign
    image = func.filter(onlineResources, "function", "browseGraphic")
    code = func.filter(onlineResources, "function", "code")
    websites = func.filter(onlineResources, "function", "website")
    documents = func.filter(onlineResources, "function", "documentation")
  >
</#if>
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

  <@b.metadataContainer "nerc-model nerc-modelcode" >

    <@b.admin/>

    <div class="model--header">
      <#if title?? && title?has_content>
        <h1>
          ${title}
          <#if (version?? && version?has_content) || (releaseDate?? && releaseDate?has_content)>
            <small><br>
              <#if version?? && version?has_content>version ${version}</#if>
              <#if releaseDate?? && releaseDate?has_content>(${releaseDate})</#if>
            </small>
            </#if>
        </h1>
      </#if>
    </div>
    
    <div class="model--body">
    
      <#if (code?? && code?has_content) ||(documents?? && documents?has_content)>
        <div class="model--body--access">
          <div>Access</div>
          <#if code?? && code?has_content>
            <#list code as link>
              <#if link.url?? && link.url?has_content>
                <@m.urlAccess link "code">
                  <span class="fas fa-code fa-lg"></span> 
                  <span>
                  <#if link.name?? && link.name?has_content>
                    ${link.name}
                  <#else>
                    Access the code
                  </#if>
                  </span>
                </@m.urlAccess>
              </#if>
            </#list>
          </#if>

          <#if documents?? && documents?has_content>
            <#list documents as link>
              <#if link.url?? && link.url?has_content>
                <@m.urlAccess link "documentation">
                  <span class="fas fa-book fa-lg"></span>
                  <span>
                  <#if link.name?? && link.name?has_content>
                    ${link.name}
                  <#else>
                    Access documentation
                  </#if>
                  </span>
                </@m.urlAccess>
              </#if>
            </#list>
          </#if>    
        </div>
      </#if>

      <div class="model--body--main">
        <#if description?? && description?has_content>
          <div class="description clearfix">
            <#if image?? && image?has_content>
              <#if image?first.url?matches("^http(|s)://.+(jpg|jpeg|gif|png)$")>
                <img src="${image?first.url}" alt="${image?first.name}" class="browseGraphic" />
              </#if>
            </#if>
            <@b.linebreaks description />
          </div>
        </#if>

        <#if purpose?? && purpose?has_content>
          <@m.key "Purpose">
            <@b.linebreaks purpose />
          </@m.key>
        </#if>

        <#if modelType?? && modelType?has_content>
          <@m.key "Model type" >${modelType?cap_first}</@m.key>
        </#if>

        <@m.key "Contacts">
          <#if responsibleParties?? && responsibleParties?has_content>
            <#list responsibleParties as responsibleParty>
              <dt>${responsibleParty.roleDisplayName}</dt>
              <dd>
                <div class="responsibleParty">
                  <@m.displayContact responsibleParty, true />
                </div>
              </dd>
              </#list>
          <#else>
            <p class="text-danger">No contacts listed for this model</p>
          </#if>
        </@m.key>

        <#if licenseType?? && licenseType?has_content>
          <@m.key "Licence">${licenseType?cap_first}</@m.key>
        </#if>


        <#if keyInputVariables?? || keyOutputVariables?? || calibration?? || configuration??>
          <div>
            <h2>Model Description</h2>
            <#if calibration?? && calibration?has_content>
              <@m.key "Calibration">${calibration}</@m.key>
            </#if>
            <#if configuration?? && configuration?has_content>
              <@m.key "Configuration">${configuration}</@m.key>
            </#if>
            <#if keyInputVariables?? && keyInputVariables?has_content>
              <@m.key "Key input variables"><@m.commaList keyInputVariables /></@m.key>
            </#if>
            <#if keyOutputVariables?? && keyOutputVariables?has_content>
              <@m.key "Key output variables" ><@m.commaList keyOutputVariables /></@m.key>
            </#if>
          </div>
          </#if>

        <#if spatialDomain?? || boundingBoxes?? || resolution?has_content>
        <div>
          <h2>Resolution/scale</h2>

          <#if resolution?? && resolution?has_content>
            <#list resolution as item>
              <div class="key-value">
                <#if item.category?has_content><div class="key">${item.category?cap_first}</div></#if>
                <div class="value">
                  <#if item.min?has_content>Min: ${item.min}</#if>
                  <#if item.max?has_content>Max: ${item.max}</#if>
                </div>
              </div>
            </#list>
          
          </#if>

          <#if boundingBoxes?has_content>
            <@m.key "Spatial extent">
              <div id="studyarea-map">
                <#list boundingBoxes as extent>
                  <span content="${extent.wkt}" datatype="geo:wktLiteral"/>
                </#list>
              </div>
            </@m.key>
          </#if>

          <#if spatialDomain?? && spatialDomain?has_content>
            <@m.key "Spatial domain">${spatialDomain}</@m.key>
          </#if>
        </div>
        </#if>

        <#if language?? || compiler?? || operatingSystem?? || systemMemory?? || documentation??>
        <div>
          <h2>Technical information</h2>
          <#if language?? && language?has_content>
            <@m.key "Language" "Computing language in which the model is written">${language}</@m.key>
          </#if>
          <#if compiler?? && compiler?has_content>
            <@m.key "Compiler" "Compiled required">${compiler}</@m.key>
          </#if>
          <#if operatingSystem?? && operatingSystem?has_content>
            <@m.key "Operating system" "Operating system typically used to run the model">${operatingSystem}</@m.key>
          </#if>
          <#if systemMemory?? && systemMemory?has_content>
            <@m.key "System memory" "Memory required to run code">${systemMemory}</@m.key>
          </#if>
          <#if documentation?? && documentation?has_content>
            <@m.key "Documentation" "Location of technical documentation for the model"><@bareUrl documentation /></@m.key>
          </#if>
        </div>
        </#if>

        <div>
          <h2>Quality assurance</h2>
          <#if qa?? && qa?has_content>
            <div  class="table--qa">
            <table class="table">
              <thead>
                <tr>  
                  <th>Category</th>
                  <th>Date</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                <#list qa as item>
                <tr>
                  <td><#if item.category?has_content>${item.category}</#if></td>
                  <td><#if item.date?has_content>${item.date}</#if></td>
                  <td><#if item.notes?has_content>${item.notes}</#if></td>
                </tr>
                </#list>
              </tbody>
            </table>
            </div>
          <#else>
            <p class="text-danger">No quality assurance information provided</p>
          </#if>
        </div>

        <div>
        <#assign nercModelUses=jena.nercModelUses(uri)/>
          <#if nercModelUses?has_content>
            <h2>Implementations of the model</h2>
            <#list nercModelUses>
                <#items as md>
                  <@b.blockUrl md/>
                </#items>         
            </#list>
          </#if>
        </div>

        <#if (references?? && references?has_content) || (websites?? && websites?has_content) || (additionalInfo?? && additionalInfo?has_content)>
        <div>
          <h2>Supplemental information</h2>

          <#if websites?? && websites?has_content>
            <@m.key "Web links">
              <#list websites as link>
                <p>
                  <#if link.url?? && link.url?has_content>
                    <@m.Url link.url/>
                  </#if>
                </p>
              </#list>
            </@m.key>
          </#if>

          <#if references?? && references?has_content>
            <@m.key "References">
              <#list references as ref>
                <div><#if ref.name?? && ref.name?has_content>${ref.name}</#if></div>
                <div><#if ref.description?? && ref.description?has_content>${ref.description}</#if></div>
                <div><#if ref.url?? && ref.url?has_content><@m.Url ref.url/></#if></div>
              </#list>
            </@m.key>
          </#if>

          <#if additionalInfo?? && additionalInfo?has_content>
            <#list additionalInfo as ai>
                <@m.key ai.key>
                  ${ai.value}
                </@m.key>
            </#list>
           </#if>     

        </div>
        </#if>

        <@m.additionalMetadata />
      </div>
    </div>

  </@b.metadataContainer>
  <#include "_footer.ftlh">

</@skeleton.master>