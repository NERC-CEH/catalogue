<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="input-<%= identifier %>">
      <%= label %>
      <a data-toggle="collapse" title="Click for help" href="#help-<%= identifier %>" data-parent="#editor"><i class="glyphicon glyphicon-question-sign"></i></a>
    </label>
    <div id="help-<%= identifier %>" class="editor-help">
       <%= helpText %>
    </div>
  </div>
  <div class="col-sm-9 col-lg-9 dataentry"></div>
</div>