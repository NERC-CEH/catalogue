import _ from 'underscore'

export default _.template(`
<div
 <% if(data.required) { %>
    class='row required' aria-required='true'
  <% } else { %>
    class='row'
  <% } %>
>
    <div class="col-sm-3 datalabel">
        <% if(data.label) { %>
        <label for="input-<%= data.modelAttribute %>">
            <%= data.label %>
            <% if(data.helpText) { %>
                <a data-toggle="collapse" title="Click for help" href="#help-<%= data.modelAttribute %>" data-parent="#editor"><i class="fa-regular fa-circle-question"></i></a>
            <% } %>
        </label>
        <% } %>
        <div id="template-add-on"></div>
        <% if(data.helpText) { %>
            <div id="help-<%= data.modelAttribute %>" class="editor-help">
            <%= data.helpText %>
            </div>
        <% } %>
    </div>
    <div id="dataentry" class="col-sm-9 dataentry"></div>
</div>
`)
