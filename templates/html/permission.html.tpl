<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title="Permission for: ${title}"><#escape x as x?html>
  <div id="metadata" class="container">
    <h1>${title}</h1>
    <h2>Permissions</h2>
    <table class="table">
      <thead>
        <tr>
          <th>Username/Group</th>
          <th>Can View</th>
          <th>Can Edit</th>
          <th>Can Delete</th>
        </tr>
      </thead>
      <tfoot>
        <tr>
          <td><input type="text" placeholder="Add username or group"></td>
          <td><input type="checkbox"></td>
          <td><input type="checkbox"></td>
          <td><input type="checkbox"></td>
        </tr>  
      </tfoot>
      <tbody>
        <#list permissions?sort_by("identity") as permission>
          <tr>
            <td>${permission.identity}</td>
            <td><input type="checkbox" <#if permission.canView>checked</#if>></td>
            <td><input type="checkbox" <#if permission.canEdit>checked</#if>></td>
            <td><input type="checkbox" <#if permission.canDelete>checked</#if>></td>
          </tr>
        </#list>
      </tbody>
    </table>
</#escape></@skeleton.master>