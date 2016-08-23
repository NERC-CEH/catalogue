<#import "skeleton.html.tpl" as skeleton>
<#assign canEdit = permission.userCanEdit(id)>
<@skeleton.master title="Catalogue for: ${title!''}"><#escape x as x?html>
  <div id="metadata" class="container catalogue">
    <h1>${title!""}</h1>
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
          <button class="btn btn-primary navbar-btn"><i class="glyphicon glyphicon-edit"></i> Edit</button>
        </#if>
      </div>
    </div>
  </div>
  </div>
</#escape></@skeleton.master>