import _ from 'underscore'

export default _.template(`
<div class="form-inline" style="margin-top: 6px">
    <div class="mb-3">
        <label for="linkedDocumentId">Linked Document Identifier</label>
        <input data-name="<%= data.modelAttribute %>" class="form-control" readonly id="linkedDocumentId" value="<%= data.value %>">
    </div>
</div>
<div class="form-inline" style="margin-top: 6px">
    <div class="mb-3">
        <label for="catalogue">Catalogue</label>
        <select id="catalogue" class="form-control"></select>
    </div>
    <div class="input-group">
        <input placeholder="Search the catalogue" id="term" type="text" autocomplete="off" class="form-control" value="<%= data.term %>">
        <div class="input-group-btn">
            <button tabindex="-1" class="btn btn-success" type="button" aria-label="Search">
                <span class="fa-solid fa-search"></span>
            </button>
        </div>
    </div>
<hr>
<div id="results"></div>
`)
