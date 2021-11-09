<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="files<%= data.index %>Name">Name</label>
    </div>
    <div class="col-sm-10">
        <input data-name='name' class="editor-input" id="files<%= data.index %>Name" value="<%= data.name %>">
    </div>
</div>

<div class="col-sm-10 col-sm-offset-2 hidden-xs"><hr></div>

<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="files<%= data.index %>Format">Format</label>
    </div>
    <div class="col-sm-10">
        <input data-name='format' class="editor-input" id="files<%= data.index %>Format" value="<%= data.format %>">
    </div>
</div>

<div class="col-sm-10 col-sm-offset-2 hidden-xs"><hr></div>

<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="files<%= data.index %>Size">Size</label>
    </div>
    <div class="col-sm-10">
        <input data-name='size' class="editor-input" id="files<%= data.index %>Size" value="<%= data.size %>">
    </div>
</div>