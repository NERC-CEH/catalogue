import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2 col-lg-1">
        <label for="deployment<%= data.index %>Start">Start</label>
    </div>
    <div class="col-sm-4 col-lg-5">
        <input data-name="start" id="deployment<%= data.index %>Start"" class="editor-input deployment-start" autocomplete="off" value="<%= data.start %>">
    </div>
    <div class="col-sm-2 col-lg-1">
        <label deployment<%= data.index %>End">End</label>
    </div>
    <div class="col-sm-4 col-lg-5">
        <input data-name="end" id="deployment<%= data.index %>End" class="editor-input deployment-end" autocomplete="off" value="<%= data.end %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2 col-lg-1">
        <label class="control-label" for="deployment<%= data.index %>Conditions">Conditions</label>
    </div>
    <div class="col-sm-10 col-lg-11">
        <textarea data-name='conditions' class="editor-textarea" id="deployment<%= data.index %>Conditions" rows="3"><%= data.conditions %></textarea>
    </div>
</div>
`)
