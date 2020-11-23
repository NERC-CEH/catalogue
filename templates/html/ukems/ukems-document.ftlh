<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

<div id="metadata">
  <div class="container">
    <#include "_admin.ftlh">
    <h1>${title}</h1>
    <#if supplemental??>
      <#list supplemental as extradoc>
        <#-- this fails idk why -->
        <p><a href="${extradoc.url}">${extradoc.title} - ${extradoc.description}</a>
      </#list>
    </#if>
  </div>
</div>
</@skeleton.master>
