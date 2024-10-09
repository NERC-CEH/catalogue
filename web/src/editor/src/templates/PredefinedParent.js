import _ from 'underscore'

export default _.template(`
<div class="dropdown">
    <button class="editor-button dropdown-toggle" data-bs-toggle="dropdown" type="button" id="dropdown<%= data.modelAttribute %>Menu" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
        Add
        <span class="caret"></span>
    </button>
    <ul class="dropdown-menu" aria-labelledby="dropdown<%= data.modelAttribute %>Menu">
        <li><a class="dropdown-item" href="#">Custom</a></li>
    </ul>
</div>
`)
