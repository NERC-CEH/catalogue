<#import "../../skeleton.ftl" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(catalogueKey)>
<div class="container" id="simple-upload">
  <h1>Dataset Upload: ${title}</h1>
  <ul id="messages"></ul>
  <h2>Upload</h2>
  <form action="/upload/${id}"
        class="dropzone"
        id="simple-upload-dropzone"
  ></form>
  <h2>Files</h2>
  <div id="filesTools"></div>
  <ul id="files">
  <#list files as file>
    <li>${file.name}</li>
  </#list>
  </ul>
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