import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-3 datalabel">
        <label for="input-<%= data.modelAttribute %>">
            <%= data.label %>
            <% if(data.helpText) { %>
                <a data-toggle="collapse" title="Click for help" href="#help-<%= data.modelAttribute %>" data-parent="#editor"><i class="fa-regular fa-circle-question"></i></a>
            <% } %>
        </label>
        <div id="help-<%= data.modelAttribute %>" class="editor-help">
            <%= data.helpText %>
        </div>
    </div>
    <div id="dataentry" class="col-sm-9 dataentry"></div>
</div>
`)
