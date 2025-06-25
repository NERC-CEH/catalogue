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
      <div class="clearfix"></div>
      <% if (data.fetchKeywordsButton) { %>
        <div class="btn-group align-items-end mt-1 float-end" role="group">
          <select id="location-select" class="form-select form-select-sm w-auto">
            <option value="eidc" selected>EIDC</option>
            <option value="dropbox">Dropbox</option>
          </select>
          <button class="legilo-keywords-btn editor-button mx-lg-1 px-3">
            <i class="fa-solid fa-wand-magic-sparkles"></i> Suggest
          </button>
        </div>
      <% } %>
      <div id="help-<%= data.modelAttribute %>" class="editor-help w-100">
          <%= data.helpText %>
      </div>
    </div>
    <div class="col-md-9">
      <% if (data.fetchKeywordsButton || data.renderLegiloKeywords) { %>
         <div class="legilo-keywords-view"></div>
      <% } %>
      <div class="existing"></div>
    </div>
</div>
`)
