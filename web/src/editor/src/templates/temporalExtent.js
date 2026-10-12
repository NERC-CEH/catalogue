import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-md-1">
        <label for="temporalExtent<%= data.index %>Begin">Begin</label>
    </div>
    <div class="col-md-5">
        <input type="date" data-name="begin" id="temporalExtent<%= data.index %>Begin" class="editor-input input-start">
    </div>
    <div class="col-md-1">
        <label for="temporalExtent<%= data.index %>End">End</label>
    </div>
    <div class="col-md-5">
        <input type="date"  data-name="end" id="temporalExtent<%= data.index %>End" class="editor-input input-end">
    </div>
</div>
`)
