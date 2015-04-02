<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Error">
  <#escape x as x?html>
    <div id="metadata" class="container">
      <h1>Error</h1>
      <p>${message}</p>
    </div>
  </#escape>
</@skeleton.master>