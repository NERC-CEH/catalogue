<#import "skeleton.html.tpl" as skeleton>
<#assign canEdit = permission.userCanEdit(id)>
<@skeleton.master title="Permission for: ${title}"><#escape x as x?html>
  <div id="metadata" class="container">
    <h1>${title}</h1>
    <h2>Catalogues</h2>
    <#list catalogues?sort>
      <ul>
        <#items as catalogue>
          <li>${catalogue}</li>
        </#items>
      </ul>
    </#list>
    <div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container">
      <div class="navbar-right">
        <a class="btn btn-default navbar-btn" href="/documents/${id}">Return to metadata</a>
        <#if canEdit>
          <a class="btn btn-primary navbar-btn" href="#permission/${id}"><i class="glyphicon glyphicon-edit"></i> Edit</a>
        </#if>
      </div>
    </div>
  </div>
  </div>
</#escape></@skeleton.master>