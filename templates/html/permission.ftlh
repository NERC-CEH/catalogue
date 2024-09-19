<#import "skeleton.ftlh" as skeleton>
<#assign canEdit = permission.userCanEdit(id)>

<@skeleton.master title="Permissions" catalogue=catalogues.retrieve(catalogue) >
  <div id="metadata" data-id="${id}" class="container permission">
    <h2>Permissions</h2>
    <div>
      <p><i class="fa-solid fa-circle-info fa-2x pull-right"></i>Amend permissions for users or groups. For external users, the username is the email address with which they registered an account.<br>For UKCEH staff it is their login username <b>not</b> their email address.</p>
      <p><b>NOTE</b>: Catalogue administrators have permission to add and edit all records regardless of permissions defined here.</p>
    </div>
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Username/Group</th>
          <th>Can view</th>
          <th>Can edit</th>
          <th>Can delete</th>
          <th>Can upload</th>
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
            <td><button class="btn btn-default btn-xs" title="remove permissions for this user" disabled ><i class="fa-solid fa-xmark"></i></button></td>
          </tr>
        </#list>
      </tbody>
    </table>
    <div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container">
      <div class="navbar-right">
        <a class="btn btn-default navbar-btn" href="/documents/${id}">Return to metadata</a>
        <#if canEdit>
          <a class="btn btn-primary navbar-btn" href="#permission/${id}"><i class="fa-regular fa-edit"></i> Edit</a>
        </#if>
      </div>
    </div>
  </div>
  </div>
</@skeleton.master>
