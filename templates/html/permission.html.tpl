<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title="Permission for: ${title}"><#escape x as x?html>
  <div id="metadata" class="container">
    <h1>${title}</h1>
    <h2>Permissions</h2>
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Username/Group</th>
          <th>Can View</th>
          <th>Can Edit</th>
          <th>Can Delete</th>
        </tr>
      </thead>
      <tbody>
        <#list permissions?sort_by("identity") as permission>
          <tr>
            <td>${permission.identity}</td>
            <td><input type="checkbox" disabled <#if permission.canView>checked</#if>></td>
            <td><input type="checkbox" disabled <#if permission.canEdit>checked</#if>></td>
            <td><input type="checkbox" disabled <#if permission.canDelete>checked</#if>></td>
          </tr>
        </#list>
      </tbody>
    </table>
    <div class="pull-right">
      <a class="btn btn-default" href="${metadataHref}">Return to metadata</a>
      <a class="btn btn-primary" href="#permission/${id}">Edit</a>
    </div>
</#escape></@skeleton.master>