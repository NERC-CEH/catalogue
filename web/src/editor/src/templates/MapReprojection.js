import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-1 col-lg-1">
        <label class="control-label">EPSG:</label>
    </div>
    <div class="col-sm-2 col-lg-2">
        <div class="input-group">
            <input data-name='epsgCode' class="editor-input" value="<%= data.epsgCode %>"  <%= data.disabled%>>
            <span class="input-group-btn">
                <button class="editor-button-xs remove" type="button"  <%= data.disabled%>><span class="fa-solid fa-times" aria-hidden="true"></span></button>
            </span>
        </div>
    </div>
    <div class="col-sm-1 col-lg-1">
        <label class="control-label">Path</label>
    </div>
    <div class="col-sm-8 col-lg-8">
        <input data-name='path' class="editor-input" value="<%= data.path %>"  <%= data.disabled%>>
    </div>
</div>
`)
