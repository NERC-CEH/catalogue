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
  <div id="adminPanel" class="panel hidden-print">
    <div class="panel-heading"><p class="panel-title">Admin</p></div>
    <div class="panel-body">
      <div class="btn-group btn-group-sm btn-group-justified" role="group">
        <a href="#" class="btn btn-default edit-control" data-document-type="${metadata.documentType}">Edit</a>
        <div class="btn-group btn-group-sm" role="group">
          <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">Publish <span class="caret"></span></button>
            <ul class="dropdown-menu dropdown-menu-right">
              <li><a href="/documents/${id?html}/permission">Amend permissions</a></li>
              <li><a href="/documents/${id?html}/catalogue" class="catalogue-control">Amend catalogues</a></li>
              <li><a href="/documents/${id?html}/publication">Publication status</a></li>
            </ul>
        </div>
        <#if metadata.documentType != 'LINK_DOCUMENT'>
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
                <#if datacitable>
                  <li><a href="/documents/${id?html}/datacite.xml">View DataCite XML</a></li>
                </#if>
                <#if custodianEIDC?size gte 1>
                  <li><a href="https://jira.ceh.ac.uk/issues/?jql=labels%3D%22${id?html}%22" target="_blank">Jira issues for this resource</a></li>
                  <li><a href="http://eidc.ceh.ac.uk/metadata/${id?html}" target="_blank">DRH folder</a></li>
                </#if>
              </ul>
          </div>
        </#if>
      </div>
      <div class="mintDOI">
      <#if dataciteMintable && metadata.documentType != 'LINK_DOCUMENT'>
        <#if permission.userCanDatacite()>
          <form action="/documents/${id?html}/datacite" method="POST">
              <button type="submit" class="btn btn-danger" value="Mint DOI">Mint DOI</button>
          </form>
        <#else>
          <div class="alert alert-info"><i class="fa fa-check"></i> There is enough information to assign a DOI to this resource</div>
        </#if>
      <#else>
        <#if !datacitable >
          <div class="alert alert-danger"><i class="fa fa-exclamation"></i> Conditions for assigning a DOI have not been met <a href="http://eidc.ceh.ac.uk/administration-folder/eidc-operations-procedures/mintADOI" target="_blank"><i class="fa fa-question-circle"></i></a></div>
        </#if>
      </#if>
      </div>
    </div>
  </div>
</#if>