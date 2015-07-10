<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="input-<%= data.modelAttribute %>">
      <%= data.label %>
      <a data-toggle="collapse" title="Click for help" href="#help-<%= data.modelAttribute %>" data-parent="#editor"><i class="glyphicon glyphicon-question-sign"></i></a>
    </label>
    <div id="help-<%= data.modelAttribute %>" class="editor-help">
       <%= data.helpText %>
    </div>
  </div>
  <div class="col-sm-9 col-lg-9 dataentry"></div>
</div>