<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label>
      <%= data.label %>
      <% if(data.helpText) { %>
        <a data-toggle="collapse" title="Click for help" href="#help-<%= data.modelAttribute.replace('.', '\.') %>" data-parent="#editor"><i class="fa fa-question-circle"></i></a>
      <% } %>
    </label>
    <button class="editor-button add">Add <span class="fa fa-plus" aria-hidden="true"></span></button>
    <div id="help-<%= data.modelAttribute %>" class="editor-help">
      <%= data.helpText %>
    </div>
  </div>
  <div class="col-sm-9 col-lg-9">
    <div class="existing container-fluid"></div>
  </div>
</div>