<#import "../../skeleton.ftl" as skeleton>

<@skeleton.master title=title>
<div class="container" id="simple-upload">
  <h1>Dataset Upload: ${title}</h1>
    <#if message?has_content>
      <h2>Info</h2>
      <p>${message}</p>
    </#if>
    <#if error?has_content>
      <h2>Error</h2>
      <p>${error}</p>
    </#if>
    <div id="message"></div>
  <h2>Upload</h2>
  <form action="/upload/${id}"
        class="dropzone"
        id="simple-upload-dropzone"></form>
  <#list files>
    <h2>Files</h2>
    <ul id="files">
      <#items as file>
        <li>${file.name}</li>
      </#items>
    </ul>
  </#list>
</div>
</@skeleton.master>