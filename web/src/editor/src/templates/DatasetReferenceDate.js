import _ from 'underscore'

export default _.template(`
<div class="row referenceDates">
    <div class="col-xl-1 col-lg-2 col-md-3">
        <label for="input-creationDate">Created</label><br>
    </div>
    <div class="col-xl-3 col-lg-4 col-md-9">
        <input type="date" data-name="creationDate" id="input-creationDate" class="editor-input" autocomplete="off">
    </div>
    <div class="col-xl-1 col-lg-2 col-md-3">
        <label for="input-publicationDate">Published</label><br>
    </div>
    <div class="col-xl-3 col-lg-4 col-md-9">
        <input type="date" data-name="publicationDate" id="input-publicationDate" class="editor-input" autocomplete="off">
    </div>
    <div class="col-xl-1 col-lg-2 col-md-3">
        <label for="input-releasedDate">Release(d)</label><br>
    </div>
    <div class="col-xl-3 col-lg-4 col-md-9">
        <input type="date" data-name="releasedDate" id="input-releasedDate" class="editor-input" autocomplete="off">
    </div>
    <div class="col-xl-1 col-lg-2 col-md-3">
        <label for="input-unavailableDate">Withdrawn</label><br>
    </div>
    <div class="col-xl-3 col-lg-4 col-md-9">
        <input type="date" data-name="unavailableDate" id="input-unavailableDate" class="editor-input" autocomplete="off">
    </div>
    <div class="col-xl-1 col-lg-2 col-md-3">
        <label for="input-supersededDate">Superseded</label><br>
    </div>
    <div class="col-xl-3 col-lg-4 col-md-9">
        <input type="date" data-name="supersededDate" id="input-supersededDate" class="editor-input" autocomplete="off">
    </div>
</div>
`)
