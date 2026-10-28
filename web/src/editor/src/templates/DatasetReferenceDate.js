import _ from 'underscore'

export default _.template(`
<div class="referenceDates">
    <div>
        <div class="text-end">
            <label for="input-creationDate">Created</label>
        </div>
        <div>
            <input type="date" data-name="creationDate" id="input-creationDate" class="editor-input" autocomplete="off">
        </div>
        <div class="text-end">
            <label for="input-publicationDate">Published</label>
        </div>
        <div>
            <input type="date" data-name="publicationDate" id="input-publicationDate" class="editor-input" autocomplete="off">
        </div>
        <div class="text-end">
            <label for="input-releasedDate">Release(d)</label>
        </div>
        <div>
            <input type="date" data-name="releasedDate" id="input-releasedDate" class="editor-input" autocomplete="off">
        </div>
        <div class="text-end">
            <label for="input-unavailableDate">Withdrawn</label>
        </div>
        <div>
            <input type="date" data-name="unavailableDate" id="input-unavailableDate" class="editor-input" autocomplete="off">
        </div>
        <div class="text-end">
            <label for="input-supersededDate">Superseded</label>
        </div>
        <div>
            <input type="date" data-name="supersededDate" id="input-supersededDate" class="editor-input" autocomplete="off">
        </div>
    </div>
</div>
`)
