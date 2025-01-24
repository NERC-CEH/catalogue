import _ from 'underscore'

export default _.template(`
<div
 <% if(data.required) { %>
    class='row required' aria-required='true'
  <% } else { %>
    class='row'
  <% } %>
>
    <div class="col-md-3">
      <label for="input-<%= data.modelAttribute %>">
          <%= data.label %>
          <% if(data.helpText) { %>
              <a data-bs-toggle="collapse" title="Click for help" href="#help-<%= data.modelAttribute %>" data-parent="#editor"><i class="fa-regular fa-circle-question"></i></a>
          <% } %>
      </label>
      <button class="editor-button add" <%= data.disabled%>>Add <span class="fa-solid fa-plus" aria-hidden="true"></span></button>
      <% if (data.fetchKeywordsButton) { %>
        <button class="editor-button legilo-fetch-keywords-btn mx-lg-1 px-3"><i class="fa-solid fa-wand-magic-sparkles"></i> Suggest</button>
      <% } %>
      <% if (data.fetchVariablesButton) { %>
        <button class="editor-button legilo-fetch-variables-btn mx-lg-1 px-3"><i class="fa-solid fa-wand-magic-sparkles"></i> Suggest</button>
      <% } %>
      <div id="help-<%= data.modelAttribute %>" class="editor-help w-100">
          <%= data.helpText %>
      </div>
    </div>
    <div class="col-md-9">
      <div class="existing container-fluid"></div>
      <% if (data.fetchKeywordsButton) { %>
        <div class="d-flex justify-content-center">
          <span class="legilo-fetch-keywords-loader spinner-border text-secondary loader mx-3" role="status" style="display: none;"></span>
          <span class="legilo-fetch-keywords-loader-msg"></span>
        </div>
      <% } %>
      <% if (data.fetchVariablesButton) { %>
        <div class="d-flex justify-content-center">
          <span class="legilo-fetch-variables-loader spinner-border text-secondary loader mx-3" role="status" style="display: none;"></span>
          <span class="legilo-fetch-variables-loader-msg"></span>
        </div>
      <% } %>
    </div>
</div>
`)
