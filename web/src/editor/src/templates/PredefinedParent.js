import _ from 'underscore'

export default _.template(`
<div class="dropdown">
    <button class="editor-button dropdown-toggle" data-bs-toggle="dropdown" type="button" id="dropdown<%= data.modelAttribute %>Menu" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
        Add
        <span class="caret"></span>
    </button>
    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dropdown<%= data.modelAttribute %>Menu">
        <li><hr class="dropdown-divider"></li>
        <li><a class="dropdown-item" href="#">Other</a></li>
    </ul>
</div>
`)
