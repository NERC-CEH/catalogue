import _ from 'underscore'
import { formatBytes } from '../../../../file-size/formatBytes'

const template = _.template(`
<div class="row">
    <div class="col-sm-1">
        <label for="fileGroups<%- data.index %>Pattern">Pattern</label>
    </div>
    <div class="col-sm-5">
        <input data-name='pattern' class="editor-input" id="fileGroups<%- data.index %>Pattern" value="<%- data.pattern %>" placeholder="e.g. site1/*.csv">
    </div>
    <div class="col-sm-1">
        <label for="fileGroups<%- data.index %>Count">Files</label>
    </div>
    <div class="col-sm-2">
        <input data-name='count' class="editor-input" id="fileGroups<%- data.index %>Count" value="<%- data.count %>" inputmode="numeric" placeholder="e.g. 200">
    </div>
</div>
<div class="row">
    <div class="col-sm-1">
        <label for="fileGroups<%- data.index %>Format">Format</label>
    </div>
    <div class="col-sm-3">
        <input data-name='format' list="catalogue-formats" class="editor-input" id="fileGroups<%- data.index %>Format" value="<%- data.format %>">
    </div>
    <div class="col-sm-2">
        <label for="fileGroups<%- data.index %>EachSize">Size of each file</label>
    </div>
    <div class="col-sm-2">
        <input data-name='eachSize' class="editor-input" id="fileGroups<%- data.index %>EachSize" value="<%- eachSizeText %>" placeholder="e.g. 2-2.5 MB">
        <div class="size-error text-danger" role="alert" hidden></div>
    </div>
    <div class="col-sm-2">
        <label for="fileGroups<%- data.index %>TotalSize">Total size</label>
    </div>
    <div class="col-sm-2">
        <input data-name='totalSize' class="editor-input" id="fileGroups<%- data.index %>TotalSize" value="<%- totalSizeText %>" placeholder="e.g. 443 MB">
        <div class="size-error text-danger" role="alert" hidden></div>
    </div>
</div>
<div class="row">
    <div class="col-sm-1">
        <label for="fileGroups<%- data.index %>Description">Description</label>
    </div>
    <div class="col-sm-11">
        <input data-name='description' class="editor-input" id="fileGroups<%- data.index %>Description" value="<%- data.description %>">
    </div>
</div>
`)

/** The size of each file as a range, or as one size when every file is the same size. */
export const eachSizeText = (minBytes, maxBytes) => minBytes === maxBytes
  ? formatBytes(minBytes)
  : `${formatBytes(minBytes)} - ${formatBytes(maxBytes)}`

// The bytes are shown formatted. An older agreement's size text, which described the group as a
// whole, is shown as typed in the size of each file until that is given in bytes.
export default ({ data }) => template({
  data,
  eachSizeText: data.minBytes != null || data.maxBytes != null
    ? eachSizeText(data.minBytes ?? data.maxBytes, data.maxBytes ?? data.minBytes)
    : (data.size ?? ''),
  totalSizeText: data.totalBytes != null ? formatBytes(data.totalBytes) : ''
})
