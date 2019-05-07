<div class="provenance">
  <div class="row">
    <div class="col-sm-1">
      <label for="<%= data.modelAttribute %><%= data.index %>Activity">Activity</label>
    </div>
    <div class="col-sm-11">
      <textarea data-name="activity" id="<%= data.modelAttribute %><%= data.index %>Activity" class="editor-textarea" rows="3" placeholder="Actions, processes etc. describing how the resource was processed/came into being"><%= data.activity %></textarea>
    </div>
  </div>
  <div class="row">
    <div class="col-sm-1">
      <label for="<%= data.modelAttribute %><%= data.index %>Agent">Agent</label>
    </div>
    <div class="col-sm-11">
      <input data-name="agent" id="<%= data.modelAttribute %><%= data.index %>Agent" class="editor-input" value="<%= data.agent %>" placeholder="Person/people or organisation responsible for the activity taking place" >
    </div>
  </div>
  <div class="row">
    <div class="col-sm-1">
      <label for="<%= data.modelAttribute %><%= data.index %>Thedate">Date</label>
    </div>
    <div class="col-sm-11">
      <input data-name="thedate" id="<%= data.modelAttribute %><%= data.index %>Thedate" class="editor-input datepicker" value="<%= data.thedate %>">
    </div>
  </div>
  <!--<div class="row">
    <div class="col-sm-1">
      <label for="<%= data.modelAttribute %><%= data.index %>Entity">Entity</label>
    </div>
    <div class="col-sm-11">
      <input data-name="entity" id="<%= data.modelAttribute %><%= data.index %>Entity" class="editor-input" value="<%= data.entity %>">
    </div>
  </div>-->
</div>