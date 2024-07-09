<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as func>
<#import "../blocks.ftlh" as b>
<#import "macros.ftlh" as m>

<#if onlineResources?? && onlineResources?has_content>
  <#assign
    images = func.filter(onlineResources, "function", "browseGraphic")
    code = func.filter(onlineResources, "function", "code")
    websites = func.filter(onlineResources, "function", "website")
    documents = func.filter(onlineResources, "function", "documentation")
  >
</#if>
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) footer=true>


 <div id="metadata" class="nerc-model nerc-modelcode">
    <div class="section section-grey">
      <div class="container">

        <#if title?? && title?has_content>
          <div id="pageTitle">
            <@b.admin/>
            <h1>
            <#if (metadata.state == 'draft' || metadata.state == 'pending') >
              <small class="text-danger"><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></small>
            </#if>
            ${title}
             <#if (version?? && version?has_content) || (releaseDate?? && releaseDate?has_content)>
                <small><br>
                  <#if version?? && version?has_content>version ${version}</#if>
                  <#if releaseDate?? && releaseDate?has_content>(${releaseDate})</#if>
                </small>
            </#if>
            </h1>
          </div>
        </#if>

        <#if description??>
          <div class="description">
            <div class="description-text">
              <@b.linebreaks description />
            </div>
            <div class="browseimage">
              <#if images?? && images?has_content>
                <img src="${images?first.url}" alt="${images?first.name}"/>
              </#if>
            </div>
          </div>
        </#if>

      </div>
    </div>

    <div class="section">
      <div class="container">
        <div class="metadata-grid">
          <div class="col1">

            <div>
              <h2 class="hideLarge">Fundamental information</h2>
              <#if purpose?? && purpose?has_content>
                <@m.key "Purpose">
                  <@b.linebreaks purpose />
                </@m.key>
              </#if>

              <#if modelType?? && modelType?has_content>
                <@m.key "Model type" >${modelType?cap_first}</@m.key>
              </#if>

              <#if licenseType?? && licenseType?has_content>
                <@m.key "Licence">${licenseType?cap_first}</@m.key>
              </#if>
            </div>

            <#if calibration?? || configuration?? || language?? || compiler?? || operatingSystem?? || systemMemory?? || documentation?? || (additionalTechnicalInfo?? && additionalTechnicalInfo?has_content)>
              <div>
                <h2>Technical information</h2>
                <#if calibration?? && calibration?has_content>
                  <@m.key "Calibration">${calibration}</@m.key>
                </#if>
                <#if configuration?? && configuration?has_content>
                  <@m.key "Configuration">${configuration}</@m.key>
                </#if>
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
                <#if additionalTechnicalInfo?? && additionalTechnicalInfo?has_content>
                   <#list additionalTechnicalInfo as ati>
                    <@m.key ati.key>
                      ${ati.value}
                    </@m.key>
                  </#list>
                </#if>
              </div>
            </#if>

            <#if inputParameters?? || outputParameters?? >
              <div>
                <h2>Model parameters</h2>
                <#if inputParameters?? && inputParameters?has_content>
                  <@m.key "Input parameters">
                    <@m.parameters inputParameters />
                  </@m.key>
                </#if>
                <#if outputParameters?? && outputParameters?has_content>
                  <@m.key "Output parameters">
                    <@m.parameters outputParameters />
                  </@m.key>
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
                        <span content="${extent.bounds}" datatype="geoJson"/>
                      </#list>
                    </div>
                  </@m.key>
                </#if>

                <#if spatialDomain?? && spatialDomain?has_content>
                  <@m.key "Spatial domain">${spatialDomain}</@m.key>
                </#if>
              </div>
            </#if>

            <div>
              <h2>Quality assurance</h2>
              <#if qa?? && qa?has_content>
                <div class="table--qa">
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

            <#assign nercModelUses=jena.nercModelUses(uri)/>
              <#if nercModelUses?has_content>
                <div>
                  <h2>Implementations of this model</h2>
                  <#list nercModelUses>
                      <#items as modelUse>
                        <@b.blockUrl modelUse/>
                      </#items>
                  </#list>
                </div>
              </#if>

            <#if references?? && references?has_content>
              <div class="references">
                <h2>References</h2>
                  <#list references as ref>
                    <div class="reference">
                      <#if ref.name?? && ref.name?has_content>$<div>{ref.name}</div></#if>
                      <#if ref.description?? && ref.description?has_content><div>${ref.description}</div></#if>
                      <#if ref.url?? && ref.url?has_content><div><@m.Url ref.url/></div></#if>
                    </div>
                  </#list>
              </div>
            </#if>

            <#if (websites?? && websites?has_content) || (additionalInfo?? && additionalInfo?has_content)>
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
          <div class="col2">

            <#if (code?? && code?has_content) || (documents?? && documents?has_content)>
              <div class="accessLinks">
                <#if code?? && code?has_content>
                  <#list code as link>
                    <#if link.url?? && link.url?has_content>
                      <@m.urlAccess link "code">
                        <div><i class="fa-solid fa-code fa-lg"></i></div>
                        <div>
                        <#if link.name?? && link.name?has_content>
                          ${link.name}
                        <#else>
                          Access the code
                        </#if>
                        </div>
                      </@m.urlAccess>
                    </#if>
                  </#list>
                </#if>

                <#if documents?? && documents?has_content>
                  <#list documents as link>
                    <#if link.url?? && link.url?has_content>
                      <@m.urlAccess link "documentation">
                        <div><i class="fa-solid fa-book fa-lg"></i></div>
                        <div>
                        <#if link.name?? && link.name?has_content>
                          ${link.name}
                        <#else>
                          Access documentation
                        </#if>
                        </div>
                      </@m.urlAccess>
                    </#if>
                  </#list>
                </#if>
              </div>
            </#if>

            <#if responsibleParties?? && responsibleParties?has_content>
              <div>
                <h2>Contacts</h2>
                  <#list responsibleParties as responsibleParty>
                    <dt>${responsibleParty.roleDisplayName}</dt>
                    <dd>
                      <div class="responsibleParty">
                        <@m.displayContact responsibleParty, true />
                      </div>
                    </dd>
                    </#list>
                  </div>
                <#else>
                  <p class="text-danger">There are no contacts listed for this model</p>
                </#if>


          </div>
        </div>
      </div>
    </div>

  </div>

</@skeleton.master>
