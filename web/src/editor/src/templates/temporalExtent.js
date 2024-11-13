import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-md-3">
        <label for="input-creationDate">Created</label>
    </div>
    <div class="col-md-9">
        <input type="date" data-name="creationDate" id="input-creationDate" class="editor-input">
    </div>
</div>
<div class="row">
    <div class="col-md-3">
        <label for="input-modificationDate">Modification Date</label>
    </div>
    <div class="col-md-9">
        <input type="date" data-name="modificationDate" id="input-modificationDate" class="editor-input">
    </div>
</div>
<div id="provenanceContributors"></div>
`)
