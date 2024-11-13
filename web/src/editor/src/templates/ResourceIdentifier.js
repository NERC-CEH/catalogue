import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-lg-1">
        <label for="resourceIdentifier<%= data.index %>Code">Code</label>
    </div>
    <div class="col-lg-5">
        <input data-name="code" id="resourceIdentifier<%= data.index %>Code" class="editor-input" value="<%= data.code %>" <%= data.disabled%>>
    </div>
    <div class="col-lg-2">
        <label for="resourceIdentifier<%= data.index %>CodeSpace">Codespace</label>
    </div>
    <div class="col-lg-2">
        <input data-name="codeSpace" id="resourceIdentifier<%= data.index %>CodeSpace" class="editor-input" value="<%= data.codeSpace %>" <%= data.disabled%>>
    </div>
    <div class="col-lg-1">
        <label for="resourceIdentifier<%= data.index %>Version">Version</label>
    </div>
    <div class="col-lg-1">
        <input data-name="version" id="resourceIdentifier<%= data.index %>Version" class="editor-input" value="<%= data.version %>" <%= data.disabled%>>
    </div>
</div>
`)
