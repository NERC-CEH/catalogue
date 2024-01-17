import _ from 'underscore'

export default _.template(`
<div class="row" id="input<%= data.modelAttribute %><%= data.index %>">
    <div class="col-sm-11 dataentry">
        <input data-index="<%= data.index %>" class="editor-input" value="<%= data.value %>" placeholder="<%= data.placeholderAttribute %>" <%= data.disabled%> >
    </div>
    <div class="col-sm-1">
        <button data-index="<%= data.index %>" class="editor-button-xs remove" <%= data.disabled%>><i class="fa-solid fa-times"></i></button>
    </div>
</div>
`)
