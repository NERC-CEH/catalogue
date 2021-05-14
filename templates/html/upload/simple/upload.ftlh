<#import "../../skeleton.ftlh" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(catalogueKey)>
<div class="container" id="simple-upload">
  <h1>${title}</h1>
  <a href="/documents/${id}">Return to metadata</a>
  <div id="messages">
    <h2>Messages</h2>
    <div id="messages-tools"></div>
    <ul id="messages-list"></ul>
  </div>
  <div id="upload">
    <h2>Upload</h2>
    <form action="/upload/${id}" id="simple-upload-dropzone">
    </form>
  </div>
  <div id="files">
    <h2>Files</h2>
    <div id="files-tools"></div>
    <ul id="files-list">
    <#list files as file>
      <li>${file.name}</li>
    </#list>
    </ul>
  </div>
</div>
<script id="files-data" type="application/json">
[<#list files as file>
  {"name":"${file.name}","urlEncodedName":"${file.urlEncodedName}"}<#sep>,</#sep>
</#list>]
</script>
<#if message?? >
<script id="messages-data" type="application/json">
{"message":"${message.message}","type": "${message.type}"}
</script>
</#if>
</@skeleton.master>