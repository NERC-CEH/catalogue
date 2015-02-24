<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title="Permission for: ${title}"><#escape x as x?html>
  <div id="metadata" class="container permission">
    <h1>${title}</h1>
    <h2>Permissions</h2>
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Username/Group</th>
          <th>Can View</th>
          <th>Can Edit</th>
          <th>Can Delete</th>
          <th>Action</th>
        </tr>
      </thead>
      <tbody>
        <#list permissions?sort_by("identity") as permission>
          <tr>
            <td>${permission.identity}</td>
            <td><input type="checkbox" disabled <#if permission.canView>checked</#if>></td>
            <td><input type="checkbox" disabled <#if permission.canEdit>checked</#if>></td>
            <td><input type="checkbox" disabled <#if permission.canDelete>checked</#if>></td>
            <td><button class="btn btn-default btn-xs" disabled ><i class="glyphicon glyphicon-remove"></i></button></td>
          </tr>
        </#list>
      </tbody>
    </table>
    <div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container">
      <div class="navbar-right">
        <a class="btn btn-default navbar-btn" href="${metadataHref}">Return to metadata</a>
        <a class="btn btn-primary navbar-btn" href="#permission/${id}"><i class="glyphicon glyphicon-edit"></i> Edit</a>
      </div>
    </div>
  </div>
  </div>
</#escape></@skeleton.master>