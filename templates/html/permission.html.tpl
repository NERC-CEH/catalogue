<#import "skeleton.html.tpl" as skeleton>
<#assign canEdit = permission.userCanEdit(id)>
<@skeleton.master title="Permissions" catalogue=catalogues.retrieve(catalogue)><#escape x as x?html>
  <div id="metadata" class="container permission">
    <h2>Permissions</h2>
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Username/Group</th>
          <th>Can View</th>
          <th>Can Edit</th>
          <th>Can Delete</th>
          <th>Can Upload</th>
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
            <td><input type="checkbox" disabled <#if permission.canUpload>checked</#if>></td>
            <td><button class="btn btn-default btn-xs" disabled ><i class="fa fa-times"></i></button></td>
          </tr>
        </#list>
      </tbody>
    </table>
    <div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container">
      <div class="navbar-right">
        <a class="btn btn-default navbar-btn" href="/documents/${id}">Return to metadata</a>
        <#if canEdit>
          <a class="btn btn-primary navbar-btn" href="#permission/${id}"><i class="fa fa-pencil-square-o"></i> Edit</a>
        </#if>
      </div>
    </div>
  </div>
  </div>
</#escape></@skeleton.master>