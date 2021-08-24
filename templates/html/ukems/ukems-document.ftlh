<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

<div id="metadata">
  <div class="container">
    <#include "_admin.ftlh">
    <h1>${title}</h1>
    <p>${description}</p>
  </div>
</div>

</@skeleton.master>
