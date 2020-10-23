<#import "../../skeleton.ftl" as skeleton>

<@skeleton.master title=title>
<div class="container">
  <h1>${title}</h1>
  <#if message?has_content>
    <h2>Info</h2>
    <p>${message}</p>
  </#if>
  <#if error?has_content>
    <h2>Error</h2>
    <p>${error}</p>
  </#if>
  <form method="POST" enctype="multipart/form-data" action="/documents/${id}/add-upload-document">
    <input type="file" name="file" />
    <input type="submit" value="Upload" />
  </form>
  <#list files>
    <ul>
      <#items as file>
        <li>${file.name} <a href="${file.deleteUrl}">Delete</a></li>
      </#items>
    </ul>
  </#list>
</div>
</@skeleton.master>