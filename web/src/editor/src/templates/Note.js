import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-2">
        <label for="note<%= data.index %>_addedDate">Date</label>
    </div>
    <div class="col-xl-4 col-lg-10">
        <input data-name='addedDate' type="date" class="editor-input" id="note<%= data.index %>_addedDate" value="<%= data.addedDate %>">
    </div>
    <div class="col-2">
        <label for="note<%= data.index %>_addedBy">Added by</label>
    </div>
    <div class="col-xl-4 col-md-10">
        <input data-name='addedBy' class="editor-input" id="note<%= data.index %>_addedBy" value="<%= data.addedBy %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="note<%= data.index %>_Note">Note</label>
    </div>
    <div class="col-sm-10">
        <textarea data-name='note' class="editor-textarea" id="note<%= data.index %>_Note" rows="4"><%= data.note %></textarea>
    </div>
</div>
`)
