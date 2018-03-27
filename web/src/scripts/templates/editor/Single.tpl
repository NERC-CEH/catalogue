<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="input-<%= data.modelAttribute %>">
      <%= data.label %>
      <% if(data.helpText) { %>
        <a data-toggle="collapse" title="Click for help" href="#help-<%= data.modelAttribute.replace('.', '\.') %>" data-parent="#editor"><i class="fas fa-question-circle"></i></a>
      <% } %>
    </label>
    <div id="help-<%= data.modelAttribute %>" class="editor-help">
       <%= data.helpText %>
    </div>
  </div>
  <div class="col-sm-9 col-lg-9 dataentry"></div>
</div>