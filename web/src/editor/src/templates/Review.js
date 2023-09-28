import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-xs-2 col-sm-1">
    <label for="<%= data.modelAttribute %><%= data.index %>_date">Date</label>
  </div>
  <div class="col-xs-10 col-sm-4 col-md-3">
    <input data-name="reviewDate" id="<%= data.modelAttribute %><%= data.index %>_date" class="editor-input" value="<%= data.reviewDate %>" type="date">
  </div>
  <div class="col-xs-2 col-sm-2 col-md-1">
    <label for="<%= data.modelAttribute %><%= data.index %>_process">Process</label>
  </div>
  <div class="col-xs-10 col-sm-5 col-md-7">
    <input data-name="reviewProcess" id="<%= data.modelAttribute %><%= data.index %>_process" class="editor-input" value="<%= data.reviewProcess %>">
  </div>
</div>
`)
