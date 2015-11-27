<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Metadata File Upload"><#escape x as x?html>
<div class="container">
  <div class="row">
    <div class="col-sm-offset-2 col-sm-8">
      <div class="page-header">
        <h3 class="text-center">Metadata File Upload</h3>
      </div>
      <form class="form-horizontal" method="POST" enctype="multipart/form-data" action="/documents">
        <div class="form-group">
          <label for="fileUpload" class="col-sm-3 control-label">File to upload:</label>
          <div class="col-sm-9">
            <input id="fileUpload" type="file" name="file" class="form-control" accept="text/xml, application/xml">
          </div>
        </div>
        <div class="form-group">
          <label for="metadataType" class="col-sm-3 control-label">Metadata Type:</label>
          <div class="col-sm-9">
            <select id="metadataType" name="type" class="form-control">
              <option value="EF_DOCUMENT">EF</option>
              <option value="GEMINI_DOCUMENT" selected="">GEMINI</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <div class="col-sm-offset-2 col-sm-10">
            <input class="btn btn-default" type="submit" value="Upload">
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
</#escape></@skeleton.master>