import _ from 'underscore'

export default _.template(`
<div
 <% if(data.required) { %>
    class='row required' aria-required='true'
  <% } else { %>
    class='row'
  <% } %>
>
    <div class="col-md-3 datalabel">
        <% if(data.label) { %>
        <label<% if (labelFor) { %> for="<%- labelFor %>"<% } %>>
            <%= data.label %>
            <% if(data.helpText) { %>
                <a data-bs-toggle="collapse" title="Click for help" href="#help-<%- data.modelAttribute %>" data-parent="#editor"><i class="fa-regular fa-circle-question"></i></a>
            <% } %>
        </label>
        <% } %>
        <div class="template-add-on"></div>
        <% if(data.helpText) { %>
            <div id="help-<%- data.modelAttribute %>" class="editor-help">
            <%= data.helpText %>
            </div>
        <% } %>
    </div>
    <div class="col-md-9 dataentry"></div>
</div>
`)
