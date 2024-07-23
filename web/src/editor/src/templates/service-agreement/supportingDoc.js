import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-1">
        <label class="control-label" for="supportingDocs<%= data.index %>Name">
            Name
            <span class="required-field-icon" title="Required"><i class="fa fa-pencil" aria-hidden="true"></i></span>
        </label>
    </div>
    <div class="col-sm-3">
        <input data-name="name" class="editor-input" id="supportingDocs<%= data.index %>Name" value="<%= data.name %>">
    </div>
    <div class="col-sm-1">
        <label class="control-label" for="supportingDocs<%= data.index %>Format">
          Format
          <span class="required-field-icon" title="Required"><i class="fa fa-pencil" aria-hidden="true"></i></span>
        </label>
    </div>
    <div class="col-sm-3">
        <input data-name="format" class="editor-input autocomplete" id="supportingDocs<%= data.index %>Format" value="<%= data.format %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-1">
        <label class="control-label">Content</label>
    </div>
    <div class="col-sm-7" id="supportingDocs<%= data.index %>Content"></div>
</div>
`)
