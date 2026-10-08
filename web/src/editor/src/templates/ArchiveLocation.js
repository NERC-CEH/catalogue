import _ from 'underscore'

export default _.template(`
<div class="row py-1">
    <div class="col-xl-2 col-lg-3 col-md-4">
        <label for="input-archive">Archive (building)</label>
    </div>
    <div class="col-xl-10 col-lg-9 col-md-8">
        <input data-name="archive" id="input-archive" class="editor-input">
    </div>
</div>
<div class="row py-1">
    <div class="col-xl-2 col-lg-3 col-md-4">
        <label for="input-locale">Locale</label>
    </div>
    <div class="col-xl-10 col-lg-9 col-md-8">
        <input data-name="locale" id="input-locale" class="editor-input">
    </div>
</div>
<div class="row py-1">
    <div class="col-xl-2 col-lg-3 col-md-4">
        <label for="input-shelf">Shelf</label>
    </div>
    <div class="col-xl-10 col-lg-9 col-md-8">
        <input data-name="shelf" id="input-shelf" class="editor-input">
    </div>
</div>
`)
