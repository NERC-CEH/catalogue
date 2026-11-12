import _ from 'underscore'
import { formatBytes } from '../../../../file-size/formatBytes'

const template = _.template(`
<div class="row">
    <div class="col-sm-1">
        <label for="files<%- data.index %>Name">Path</label>
    </div>
    <div class="col-sm-3">
        <input data-name='name' class="editor-input" id="files<%- data.index %>Name" value="<%- data.name %>">
    </div>
    <div class="col-sm-1">
        <label for="files<%- data.index %>Format">Format</label>
    </div>
    <div class="col-sm-3">
        <input data-name='format' list="catalogue-formats" class="editor-input" id="files<%- data.index %>Format" value="<%- data.format %>">
    </div>
    <div class="col-sm-1">
        <label for="files<%- data.index %>Size">Size</label>
    </div>
    <div class="col-sm-3">
        <input data-name='sizeEntry' class="editor-input" id="files<%- data.index %>Size" value="<%- sizeText %>" placeholder="e.g. 32 MB">
        <div class="size-error text-danger" role="alert" hidden></div>
    </div>
</div>
`)

// The bytes are shown formatted; an older agreement's size text is shown as typed until it is replaced
export default ({ data }) => template({
  data,
  sizeText: data.bytes != null ? formatBytes(data.bytes) : (data.size ?? '')
})
