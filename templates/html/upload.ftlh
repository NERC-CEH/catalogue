<#import "skeleton.ftlh" as skeleton>

<#assign uploadScripts>
  <script src="/static/scripts/editor-bundle.js"></script>
</#assign>

<@skeleton.master title="Metadata File Upload" catalogue=catalogues.defaultCatalogue() scripts=uploadScripts>
<div class="container">
  <div class="row">
    <div class="col-sm-offset-2 col-sm-8">
      <div class="page-header">
        <h3 class="text-center">Metadata File Upload</h3>
      </div>
      <form class="form-horizontal" method="POST" enctype="multipart/form-data" action="/documents">
        <div class="mb-3">
          <label for="fileUpload" class="col-sm-3 ">File to upload:</label>
          <div class="col-sm-9">
            <input id="fileUpload" type="file" name="file" class="form-control" accept="text/xml, application/xml">
          </div>
        </div>
        <div class="mb-3">
          <label for="metadataType" class="col-sm-3 ">Metadata Type:</label>
          <div class="col-sm-9">
            <select id="metadataType" name="type" class="form-control">
              <option value="EF_DOCUMENT">EF</option>
              <option value="GEMINI_DOCUMENT" selected="">GEMINI</option>
            </select>
          </div>
        </div>
        <div class="mb-3">
          <label for="catalogue" class="col-sm-3 ">Catalogue:</label>
          <div class="col-sm-9">
            <select id="catalogue" name="catalogue" class="form-control">
              <#list catalogues.retrieveAll() as catalogue>
                <option value="${catalogue.id}">${catalogue.title}</option>
              </#list>
            </select>
          </div>
        </div>
        <div class="mb-3">
          <div class="col-sm-offset-2 col-sm-10">
            <input class="btn btn-light border" type="submit" value="Upload">
          </div>
        </div>
      </form>
      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">File Upload Information</h3>
        </div>
        <div class="panel-body">
          Supported File Types:
          <ul>
            <li>GEMINI XML</li>
            <li>EF XML</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</div>
</@skeleton.master>
