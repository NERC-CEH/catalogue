<#function filter things name value >
    <#local result = []>
    <#list things as thing>
        <#if thing[name] == value >
            <#local result = result + [thing]>
        </#if>
    </#list>
    <#return result>
</#function>
<#assign custodianEIDC = filter(filter(otherContacts, "role", "custodian"), "organisationName", "Environmental Information Data Centre") >

<#if permission.userCanEdit(id)>
  <div class="row" id="adminPanel">
    <div class="col-sm-5 col-sm-offset-7 col-xs-12 col-xs-offset-0">
        <div class="btn-group btn-group-sm btn-group-justified" role="group">
          <a href="#" class="btn btn-default edit-control" data-document-type="${metadata.documentType}"><i class="fas fa-pencil-alt"></i> Edit</a>
          <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
            <div class="btn-group btn-group-sm" role="group">
              <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">Publish <span class="caret"></span></button>
                <ul class="dropdown-menu dropdown-menu-right">
                  <li><a href="/documents/${id?html}/permission">Amend permissions</a></li>
                  <li><a href="/documents/${id?html}/publication">Publication status</a></li>
                  <li><a href="/documents/${id?html}/catalogue" class="catalogue-control">Amend catalogues</a></li>
                </ul>
            </div>

            <#if metadata.documentType != 'LINK_DOCUMENT'>
              <#if metadata.catalogue == "eidc" && (resourceType.value == 'dataset' | resourceType.value == 'nonGeographicDataset' | resourceType.value == 'application')>


                <#assign menuLabel="DOI">
                <#if dataciteMintable>
                  <#assign menuLabel="Mint DOI">
                <#else>
                  <#if !datacitable>
                    <#assign menuLabel="<i class='fas fa-exclamation-triangle text-red'></i> DOI">
                  </#if>
                </#if>

                <div class="btn-group btn-group-sm" role="group">
                  <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">${menuLabel} <span class="caret"></span></button>
                    <ul class="dropdown-menu dropdown-menu-right">
                      <#if dataciteMintable>
                        <li><div class="msg text-success"><i class="fas fa-check text-success"></i> There is enough information<br>to assign a DOI to this resource</div></li>
                        <#if permission.userCanDatacite()>
                          <form action="/documents/${id?html}/datacite" method="POST" class="mintDOI">
                              <button type="submit" class="btn btn-sm btn-success" value="Mint DOI">Mint DOI</button>
                          </form>
                        </#if>
                        <li class="divider"></li>
                      <#else>
                        <#if !datacitable >
                          <li><div class="msg text-red"><i class="fas fa-exclamation"></i> Conditions for assigning a <br>DOI have not been met</div></li>
                          <li><a href="http://eidc.ceh.ac.uk/administration-folder/eidc-operations-procedures/mintADOI" target="_blank" rel="noopener noreferrer">Help</a></li>
                        </#if>
                      </#if>
                      <#if datacitable>
                        <li><a href="/documents/${id?html}/datacite.xml">View DataCite XML</a></li>
                      </#if>
                    </ul>
                </div>
              </#if>

              <div class="btn-group btn-group-sm" role="group">
                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">More <span class="caret"></span></button>
                  <ul class="dropdown-menu dropdown-menu-right">
                    <#if onlineResources?has_content>
                      <#list onlineResources as testLink>
                        <#if testLink.function == "order">
                          <li><a href="${testLink.url?html}&test=true">Test ${testLink.name}</a></li>
                        </#if>
                      </#list>
                    </#if>
                    <#if custodianEIDC?size gte 1>
                      <li><a href="https://jira.ceh.ac.uk/issues/?jql=labels%3D%22${id?html}%22" target="_blank" rel="noopener noreferrer">Jira issues for this resource</a></li>
                      <li><a href="http://eidc.ceh.ac.uk/metadata/${id?html}" target="_blank" rel="noopener noreferrer">DRH folder</a></li>
                    </#if>
                  </ul>
              </div>
            </#if>
          </#if>
        </div>         
    </div>
  </div>
</#if>