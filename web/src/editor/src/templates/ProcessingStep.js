import _ from 'underscore'

export default _.template(`
<div class="provenance">
  <div class="row">
    <div class="col-sm-12">
      <textarea data-name="step" id="<%= data.modelAttribute %><%= data.index %>Step" class="editor-textarea" rows="5" placeholder="Actions, processes etc. describing how the resource was processed/came into being"><%= data.step %></textarea>
    </div>
  </div>

</div>
`)
