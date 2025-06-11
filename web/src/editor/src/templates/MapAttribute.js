import _ from 'underscore'

export default _.template(`
<div class="border border-1 p-3">
    <h6>Attribute Definition</h6>
    <div class="row">
        <div class="col-sm-1">
            <label class="control-label">Name</label>
        </div>
        <div class="col-sm-7">
            <input data-name='id' class="editor-input" value="<%= data.id %>"  <%= data.disabled%>>
        </div>
        <div class="col-sm-2">
            <label class="control-label">Data type</label>
        </div>
        <div class="col-sm-2">
            <select data-name='type' <%= data.disabled%>>
                <% _.each(data.types, function(d) {%>
                    <option value="<%=d.value%>" <%= d.value==data.type ? 'selected="selected"': '' %>><%=d.name%></option>
                <%});%>
            </select>
        </div>
    </div>
    <h6>Layer Definition</h6>
    <div class="row">
        <div class="col-sm-1">
            <label class="control-label">ID</label>
        </div>
        <div class="col-sm-3">
            <input data-name='name' class="editor-input" value="<%= data.name %>"  <%= data.disabled%>>
        </div>
        <div class="col-sm-1">
            <label class="control-label">Title</label>
        </div>
        <div class="col-sm-7">
            <input data-name='label' class="editor-input" value="<%= data.label %>"  <%= data.disabled%>>
        </div>
    </div>

    <hr/>
    <h6>Values <button class="editor-button-xs addValue"  <%= data.disabled%>>Add <span class="fa-solid fa-plus"></span></button></h6>
    <div class="values"></div>
    <hr/>
    <h6>Buckets <button class="editor-button-xs addBucket"  <%= data.disabled%>>Add <span class="fa-solid fa-plus"></span></button></h6>
    <div class="buckets"></div>
</div>
`)
