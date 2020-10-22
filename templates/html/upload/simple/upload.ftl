<#import "../../skeleton.ftl" as skeleton>

<@skeleton.master title=title>
<div class="container">
  <p>Hello World</p>
  <p>Upload Simple: ${title}</p>
  <p>${id}</p>
  <#if message?has_content>
    <p>${message}</p>
  </#if>
  <form method="POST" enctype="multipart/form-data" action="/documents/${id}/add-upload-document">
    <input type="file" name="file" />
    <input type="submit" value="Upload" />
  </form>
</div>
</@skeleton.master>