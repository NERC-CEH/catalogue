<#assign canEdit = permission.userCanEdit(id)>
<#assign canMakePublic = permission.userCanMakePublic()>
<#if canEdit || canMakePublic>
  <div class="panel panel-default hidden-print edit-control">
    <div class="panel-heading"><p class="panel-title">Manage Metadata</p></div>
    <div class="panel-body">
      <#if canEdit>
        <p><a href="#edit/${id?html}">Edit&hellip;</a><p>
      </#if>
      <p><a href="/documents/${id?html}/permission">Permissions&hellip;</a></p>
      <#if canEdit || canMakePublic>
        <p><a href="/documents/${id?html}/publication">Publish&hellip;</a></p>
      </#if>
      <#if citation??> 
        <p><a href="/documents/${id?html}/datacite.xml">Datacite XML</a></p>
      </#if>
    </div>
  </div>
</#if>