<#import "skeleton.ftl" as skeleton>
<@skeleton.master title="Access Denied"><#escape x as x?html>
  <div id="metadata" class="container">
    <h1><i class="fas fa-ban"></i> Access Denied</h1>
    <p>You don't have permission to view this page.</p>
    <#if isPublic>
      <p>Please <a href="/sso/login">login</a> and try again</p>
    </#if>
  </div>
</#escape></@skeleton.master>