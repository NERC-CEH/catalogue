<#import "../../skeleton.ftl" as skeleton>

<@skeleton.master title=title>
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
<script id="data" type="application/json">
[<#list files as file>
  {"name":"${file.name}"}<#sep>,</#sep>
</#list>]
</script>
</@skeleton.master>