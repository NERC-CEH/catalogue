import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-md-1">
        <label for="input-begin">Begin</label>
    </div>
    <div class="col-md-5">
        <input type="date" data-name="begin" id="input-begin" class="editor-input input-start">
    </div>
    <div class="col-md-1">
        <label for="input-end">End</label>
    </div>
    <div class="col-md-5">
        <input type="date"  data-name="end" id="input-end" class="editor-input input-end">
    </div>
</div>
`)
