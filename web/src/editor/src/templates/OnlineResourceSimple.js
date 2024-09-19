import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-3">
        <label for="<%= data.modelAttribute %><%= data.index %>URL">Address</label>
    </div>
    <div class="col-sm-9">
        <input data-name="url" id="<%= data.modelAttribute %><%= data.index %>URL" class="editor-input" value="<%= data.url %>"  autocomplete="off" placeholder="https://...">
    </div>
</div>
<div class="row">
    <div class="col-sm-3">
        <label for="<%= data.modelAttribute %><%= data.index %>Name">Name of site/service</label>
    </div>
    <div class="col-sm-9">
        <input data-name="name" id="<%= data.modelAttribute %><%= data.index %>Name" class="editor-input" value="<%= data.name %>" autocomplete="off">
    </div>
</div>
<div class="row">
    <div class="col-sm-3">
        <label for="<%= data.modelAttribute %><%= data.index %>Description">Description (optional)</label>
    </div>
    <div class="col-sm-9">
        <textarea data-name="description" id="<%= data.modelAttribute %><%= data.index %>Description" class="editor-textarea" rows="3"><%= data.description %></textarea>
    </div>
</div>
<datalist id="<%= data.modelAttribute %>List"><%= data.listAttribute%></datalist>
`)
