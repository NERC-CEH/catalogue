import _ from 'underscore'

export default _.template(`
<div class="row mt-2" id="input<%= data.modelAttribute %><%= data.index %>">
    <div class="col-10 dataentry">
        <input data-index="<%= data.index %>" class="editor-input" value="<%= data.value %>" placeholder="<%= data.placeholderAttribute %>" <%= data.disabled%> >
    </div>
    <div class="col-2">
        <button data-index="<%= data.index %>" class="editor-button-xs remove" <%= data.disabled%>><i class="fa-solid fa-times"></i></button>
    </div>
</div>
`)
