import _ from 'underscore'

export default _.template(`
<div class="row py-1">
    <div class="col-xl-1 col-lg-2 col-md-3">
        <label for="input-archive">Archive (building)</label>
    </div>
    <div class="col-xl-11 col-lg-10 col-md-9">
        <input data-name="archive" id="input-archive" class="editor-input">
    </div>
</div>
<div class="row py-1">
    <div class="col-xl-1 col-lg-2 col-md-3">
        <label for="input-locale">Locale</label>
    </div>
    <div class="col-xl-11 col-lg-10 col-md-9">
        <input data-name="locale" id="input-locale" class="editor-input">
    </div>
</div>
<div class="row py-1">
    <div class="col-xl-1 col-lg-2 col-md-3">
        <label for="input-shelf">Shelf</label>
    </div>
    <div class="col-xl-11 col-lg-10 col-md-9">
        <input data-name="shelf" id="input-shelf" class="editor-input">
    </div>
</div>
`)
