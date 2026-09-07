import _ from 'underscore'

export default _.template(`
<div class="row" id="input<%= data.modelAttribute %><%= data.index %>">
    <div class="col-sm-11 dataentry">
        <select data-index="<%= data.index %>">
            <option value="">- Select Spatial Representation Type -</option>
            <option value="grid">Grid (e.g. tifs and other image formats)</option>
            <option value="textTable">Tabular data (e.g. csv/spreadsheet)</option>
            <option value="tin">Triangulated Irregular Network</option>
            <option value="vector">Vector (e.g. Shape file)</option>
            <option value="video">Video</option>
        </select>
    </div>
    <div class="col-sm-1">
        <button data-index="<%= data.index %>" class="editor-button-xs remove"><i class="fa-solid fa-times"></i></button>
    </div>
</div>
`)
