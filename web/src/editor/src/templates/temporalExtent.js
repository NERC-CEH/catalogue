import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-md-1 col-lg-1">
        <label for="input-begin">Begin</label>
    </div>
    <div class="col-md-4 col-lg-4">
        <input data-name="begin" id="input-begin" class="editor-input input-begin" autocomplete="off" value="<%= data.begin %>">
    </div>
    <div class="col-md-1 col-lg-1">
        <label for="input-end">End</label>
    </div>
    <div class="col-md-4 col-lg-4">
        <input data-name="end" id="input-end" class="editor-input input-end" autocomplete="off" value="<%= data.end %>">
    </div>
</div>
`)
