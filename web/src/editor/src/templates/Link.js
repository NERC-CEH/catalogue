import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-1 col-lg-1">
        <label for="<%= data.modelAttribute %><%= data.index %>URL">URL</label>
    </div>
    <div class="col-sm-11 col-lg-11">
        <input data-name="href" id="<%= data.modelAttribute %><%= data.index %>URL" class="editor-input" value="<%= data.href %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-1 col-lg-1">
        <label for="<%= data.modelAttribute %><%= data.index %>Title">Title</label>
    </div>
    <div class="col-sm-11 col-lg-11">
        <input data-name="title" id="<%= data.modelAttribute %><%= data.index %>Title" class="editor-input" value="<%= data.title %>">
    </div>
</div>
`)
