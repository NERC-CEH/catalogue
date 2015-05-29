<#if permission.userCanEdit(id)>
  <div class="panel panel-default hidden-print">
    <div class="panel-heading"><p class="panel-title">Manage Metadata</p></div>
    <div class="panel-body">
      <p><a href="#" class="edit-control">Edit&hellip;</a><p>
      <p><a href="/documents/${id?html}/permission">Permissions&hellip;</a></p>
      <p><a href="/documents/${id?html}/publication">Publish&hellip;</a></p>
      <#if citation??> 
        <p><a href="/documents/${id?html}/datacite.xml">Datacite XML</a></p>
      </#if>
    </div>
  </div>
</#if>