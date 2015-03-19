<#if permission?? && permission.userCanEdit() >
  <div class="panel panel-default hidden-print edit-control">
    <div class="panel-heading"><p class="panel-title">Manage Metadata</p></div>
    <div class="panel-body">
      <p><a href="#edit/${id?html}">Edit&hellip;</a><p>
      <p><a href="/documents/${id?html}/publication">Publish&hellip;</a></p>
      <p><a href="/documents/${id?html}/permission">Permissions&hellip;</a></p>
      <#if citation??> 
        <p><a href="/documents/${id?html}/datacite.xml">Datacite XML</a></p>
      </#if>
    </div>
  </div>
</#if>