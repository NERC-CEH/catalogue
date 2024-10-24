import _ from 'underscore'

export default _.template(`
<div
 <% if(data.required) { %>
    class='row required' aria-required='true'
  <% } else { %>
    class='row'
  <% } %>
>
    <div class="col-sm-3">
      <label for="input-<%= data.modelAttribute %>">
          <%= data.label %>
          <% if(data.helpText) { %>
              <a data-toggle="collapse" title="Click for help" href="#help-<%= data.modelAttribute %>" data-parent="#editor"><i class="fa-regular fa-circle-question"></i></a>
          <% } %>
      </label>
      <button class="editor-button add" <%= data.disabled%>>Add <span class="fa-solid fa-plus" aria-hidden="true"></span></button>
      <div id="help-<%= data.modelAttribute %>" class="editor-help">
          <%= data.helpText %>
      </div>
    </div>
    <div class="col-sm-9">
      <div class="existing container-fluid"></div>
    </div>
</div>
`)
